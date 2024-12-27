package ru.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.security.user.CustomToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static ru.Main.datas;

/** RU: кастомный фильтр для авторизации токенами и Basic Auth(без изменений) */
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final String basicScheme = "Basic ";

    public AuthenticationFilter(RequestMatcher req, AuthenticationManager authenticationManager) {
        super(req, authenticationManager);
        System.out.println("AuthenticationFilter " + getAuthenticationManager());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UUID token = getTokenFromHeader(request);
        User user = getUserFromHeader(request);
        CustomToken requestAuthentication = setToken(token, user);
        requestAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(requestAuthentication);
    }

    /** RU: при существовании Basic Auth обновляет подписку и отдаёт авторизацию
     * @param token UUID подписки
     * @param basic Проверенный юзер из BasicAuth
     * return Авторизация */
    private CustomToken setToken(UUID token, User basic) {
        if(!datas.subscriptions.containsKey(token)) return new CustomToken();

        Subscriber sub = datas.subscriptions.get(token);
        if(basic != null) {
            sub.setLogin(basic.getUsername());
            sub.setType(null);
            return new CustomToken(basic.getPassword(), basic.getAuthorities(), sub, token.toString());
        }
        if(sub.getUser() != null) {
            return new CustomToken(sub.getUser().getPassword(), sub.getUser().getAuthorities(), sub, token.toString());
        }
        sub.setType(null);
        return new CustomToken(sub, token.toString());
    }

    /** RU: получает информацию для авторизации токенами */
    private UUID getTokenFromHeader(HttpServletRequest request) {
        String tok = request.getHeader(SecurityConfig.authTokenHeader);
        System.out.println("attemptAuthentication " + tok);
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
        String token = decodeFromBase64(header);
        int delim = token.indexOf(":");
        if (delim == -1) return null;

        User user = datas.getDbService().userByLogin(token.substring(0, delim));
        if(user == null || !user.getPassword().equals(token.substring(delim + 1))) {
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
        System.out.println("successfulAuthentication " + authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("unsuccessfulAuthentication " + failed);
    }
}
