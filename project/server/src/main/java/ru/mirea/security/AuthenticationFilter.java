package ru.mirea.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.mirea.data.SSE.Subscriber;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static ru.mirea.Main.datas;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UUID rU = UUID.randomUUID();

    public AuthenticationFilter(RequestMatcher req) {
        super(req);
        System.out.println("AuthenticationFilter ");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
        String tok = httpServletRequest.getHeader(AUTHORIZATION);
        System.out.println("attemptAuthentication " + tok);
        UUID token = Optional.ofNullable(tok)
            .map(UUID::fromString)
            .orElse(rU);
//            .orElseThrow(() -> {
//                System.out.println("attemptAuthentication orElseThrow " + tok);
//                return new BadCredentialsException("Missing Authentication Token");
//            });
        CustomToken requestAuthentication;
//        if(!authenticationProvider.subscriptions.containsKey(token)) {
//            throw new BadCredentialsException("Missing Authentication Token");
//        }
        if(datas != null && datas.subscriptions.containsKey(token)) {
            Subscriber sub = datas.subscriptions.get(token);
            if(sub.getUser() == null) {
                requestAuthentication = new CustomToken(sub, tok);
            } else {
                requestAuthentication = new CustomToken(sub.getUser().getUsername(), sub.getUser().getPassword(), sub.getUser().getAuthorities(), sub, tok);
            }
        } else {
            requestAuthentication = new CustomToken();
        }
        // Allow subclasses to set the "details" property
        requestAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(httpServletRequest));
//        requestAuthentication.setDetails(httpServletRequest, requestAuthentication);
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
