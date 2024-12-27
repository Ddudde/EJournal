package ru.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.security.user.CustomToken;

import java.util.Collection;
import java.util.Set;

@Component
public class CustomProvider implements AuthenticationProvider {

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.isAssignableFrom(CustomToken.class);
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if(authentication.isAuthenticated()) {
            System.out.println("authenticate! " + authentication);
            return authentication;
        }

        return authentication;
    }

    public static Collection<? extends GrantedAuthority> getAuthorities(Set<String> roles) {
        return roles.stream()
            .map(SimpleGrantedAuthority::new).toList();
    }
}