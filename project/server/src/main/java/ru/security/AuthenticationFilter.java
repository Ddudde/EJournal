package ru.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/** RU: кастомный фильтр для авторизации токенами и Basic Auth(без изменений) */
@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final String basicScheme = "Basic ";
    private final PasswordEncoder passwordEncoder;
    private final DBService dbService;

    public AuthenticationFilter(RequestMatcher req, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, DBService dbService) {
        super(req, authenticationManager);
        this.passwordEncoder = passwordEncoder;
        this.dbService = dbService;
        log.trace("AuthenticationFilter " + getAuthenticationManager());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final UUID token = getTokenFromHeader(request);
        final User user = getUserFromHeader(request);
        final CustomToken requestAuthentication = setToken(token, user);
        requestAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(requestAuthentication);
    }

    /** RU: при существовании Basic Auth обновляет подписку и отдаёт авторизацию
     * @param token UUID подписки
     * @param basicUser Проверенный юзер из BasicAuth
     * return Авторизация */
    private CustomToken setToken(UUID token, User basicUser) {
        if(!MainService.subscriptions.containsKey(token)) return new CustomToken();

        final SubscriberDTO sub = MainService.subscriptions.get(token);
        if(basicUser != null) {
            sub.setLogin(basicUser.getUsername());
            sub.setUserId(basicUser.getId());
            sub.setType(null);
            return new CustomToken(basicUser.getPassword(), basicUser.getAuthorities(), sub, token.toString());
        }
        final User user = dbService.userById(sub.getUserId());
        if(user != null) {
            return new CustomToken(user.getPassword(), user.getAuthorities(), sub, token.toString());
        }
        sub.setType(null);
        return new CustomToken(sub, token.toString());
    }

    /** RU: получает информацию для авторизации токенами */
    private UUID getTokenFromHeader(HttpServletRequest request) {
        final String tok = request.getHeader(SecurityConfig.authTokenHeader);
        log.debug("attemptAuthentication " + tok);
        return Optional.ofNullable(tok)
            .map(UUID::fromString)
            .orElse(UUID.randomUUID());
    }

    /** RU: получает информацию для Basic Auth */
    private User getUserFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) return null;

        header = header.trim();
        if (!header.startsWith(basicScheme) || header.length() < 7) {
            return null;
        }
        final String token = decodeFromBase64(header);
        int delim = token.indexOf(":");
        if (delim == -1) return null;

        final String login = token.substring(0, delim);
        final String password = token.substring(delim + 1);
        final User user = dbService.userByLogin(login);
        if(user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    private String decodeFromBase64(String header) {
        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
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
