package ru.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static ru.Main.datas;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UUID rU = UUID.randomUUID();

    public AuthenticationFilter(RequestMatcher req) {
        super(req);
        System.out.println("AuthenticationFilter ");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
        String tok = httpServletRequest.getHeader(SecurityConfig.authHeader);
        System.out.println("attemptAuthentication " + tok);
        UUID token = Optional.ofNullable(tok)
            .map(UUID::fromString)
            .orElse(rU);
        CustomToken requestAuthentication;
        if(datas.subscriptions.containsKey(token)) {
            Subscriber sub = datas.subscriptions.get(token);
            if(sub.getUser() == null) {
                requestAuthentication = new CustomToken(sub, tok);
            } else {
                requestAuthentication = new CustomToken(sub.getUser().getUsername(), sub.getUser().getPassword(), sub.getUser().getAuthorities(), sub, tok);
            }
        } else {
            requestAuthentication = new CustomToken();
        }
        requestAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(httpServletRequest));
        return getAuthenticationManager().authenticate(requestAuthentication);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication " + authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException failed) {
        System.out.println("unsuccessfulAuthentication " + failed);
    }
}
