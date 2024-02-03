package ru.mirea.services.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationProvider authenticationProvider;

    public AuthenticationFilter(final RequestMatcher requiresAuth) {
        super(requiresAuth);
        System.out.println("AuthenticationFilter " + requiresAuth);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
        String tok = httpServletRequest.getHeader(AUTHORIZATION);
        System.out.println("attemptAuthentication " + tok);
        UUID token = Optional.ofNullable(tok)
            .map(UUID::fromString)
            .orElseThrow(() -> {
                System.out.println("attemptAuthentication orElseThrow " + tok);
                return new BadCredentialsException("Missing Authentication Token");
            });
        if(!authenticationProvider.subscriptions.containsKey(token)) throw new BadCredentialsException("Missing Authentication Token");
        UserDetails userDetails = userService.loadUserByUsername(authenticationProvider.subscriptions.get(token).getLogin());
        Authentication requestAuthentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
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
