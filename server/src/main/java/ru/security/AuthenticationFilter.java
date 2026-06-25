package ru.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ObjectUtils;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.security.UserDTO;
import ru.data.DTO.service.ConnectDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.IJwtService;
import ru.services.interfaces.db.IDBService;
import ru.services.logic.SSEService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** RU: кастомный фильтр для авторизации токенами и Basic Auth(без изменений) */
@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final String basicScheme = "Basic ";
    private final PasswordEncoder passwordEncoder;
    private final IDBService dbService;
    private final IJwtService jwtService;

    public AuthenticationFilter(RequestMatcher req, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, IDBService dbService, IJwtService jwtService) {
        super(req, authenticationManager);
        this.passwordEncoder = passwordEncoder;
        this.dbService = dbService;
        this.jwtService = jwtService;
        log.trace("AuthenticationFilter " + getAuthenticationManager());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.trace("auth7777 " + request.getRequestURI());
        final ConnectDTO connectDTO = getSSEConnectTokenHeader(request);
        AuthToken requestAuthentication;
        if(connectDTO != null) {
            String token = Optional.ofNullable(connectDTO.subscriber())
                .map(String::valueOf)
                .orElse(null);
            requestAuthentication = new AuthToken(connectDTO.userId(), token);
        } else {
            final UserDTO userDTO = getJWTTokenFromHeader(request);
            final UUID token = getSSETokenFromHeader(request);
            final User basicUser = getUserFromBasicAuth(request);
            requestAuthentication = prepareAuthToken(token, basicUser, userDTO);
        }
        requestAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(requestAuthentication);
    }

    private UserDTO getJWTTokenFromHeader(HttpServletRequest request) {
        final String token = request.getHeader(SecurityConfig.ACCESS_TOKEN_HEADER);
        if(ObjectUtils.isEmpty(token)) return null;

        final Claims infoFromJWT = jwtService.getInfoFromJWT(token);
        final Long userId = Long.valueOf(infoFromJWT.getSubject());
        final List<String> rawRoles = infoFromJWT.get("role", List.class);
        return new UserDTO(userId, rawRoles, token);
    }

    /** RU: при существовании Basic Auth обновляет подписку и отдаёт авторизацию
     * @param token UUID подписки
     * @param basicUser Проверенный юзер из BasicAuth
     * return Авторизация */
    private AuthToken prepareAuthToken(UUID token, User basicUser, UserDTO userDTO) {
        if(!SSEService.subscriptions.containsKey(token)) {
            if(basicUser != null) {
                final String JWTToken = jwtService.generateToken(basicUser);
                return new AuthToken(basicUser.getAuthorities(), basicUser.getId(), JWTToken);
            }
            if(userDTO == null) return new AuthToken();
            return new AuthToken(getAuthorities(userDTO.roles()), userDTO.id(), userDTO.JWTToken());
        }

        final SubscriberDTO sub = SSEService.subscriptions.get(token);
        if(basicUser != null) {
            final String JWTToken = jwtService.generateToken(basicUser);
            sub.setType(null);
            return new AuthToken(basicUser.getAuthorities(), sub, token.toString(), basicUser.getId(), JWTToken);
        }
        if(userDTO == null) return new AuthToken(sub, token.toString());

        final User user = dbService.userById(userDTO.id());
        if(user != null) {
            return new AuthToken(getAuthorities(userDTO.roles()), sub, token.toString(), userDTO.id(), userDTO.JWTToken());
        }
        sub.setType(null);
        return new AuthToken(sub, token.toString());
    }

    /** RU: получает или создаёт id для SSE */
    private UUID getSSETokenFromHeader(HttpServletRequest request) {
        final String tokenSSE = request.getHeader(SecurityConfig.SSE_TOKEN_HEADER);
        log.debug("attemptAuthentication " + tokenSSE);

        return Optional.ofNullable(tokenSSE)
            .map(UUID::fromString)
            .orElse(UUID.randomUUID());
    }

    private ConnectDTO getSSEConnectTokenHeader(HttpServletRequest request) {
        final String requestParameter = request.getParameter("uuid");
        log.trace("auth5000 " + requestParameter);
        if(ObjectUtils.isEmpty(requestParameter) || requestParameter.equals("null")) return null;

        final UUID connectTokenSSE = UUID.fromString(requestParameter);
        if(connectTokenSSE != null && SSEService.connectTokens.containsKey(connectTokenSSE)) {
            return SSEService.connectTokens.remove(connectTokenSSE);
        }
        return null;
    }

    /** RU: получает информацию для Basic Auth */
    private User getUserFromBasicAuth(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.trace("auth2 " + header);
        if (header == null) return null;

        header = header.trim();
        if (!header.startsWith(basicScheme) || header.length() < 7) {
            return null;
        }
        final String token = decodeFromBase64(header);
        final int delim = token.indexOf(":");
        if (delim == -1) return null;

        final String login = token.substring(0, delim);
        log.trace("auth3 " + login);
        final String password = token.substring(delim + 1);
        final User user = dbService.userByLogin(login);
        if(user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new)
            .toList();
    }

    private String decodeFromBase64(String header) {
        final byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        try {
            return new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Failed decode base64 token BasicAuth");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("successfulAuthentication " + authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.debug("unsuccessfulAuthentication " + failed);
    }
}
