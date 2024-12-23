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

//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private MainService main;

//    @Override
//    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
//        //
//        System.out.println("additionalAuthenticationChecks " + userDetails);
//    }

//    @Override
//    protected User retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
//        Object token = usernamePasswordAuthenticationToken.getCredentials();
//        System.out.println("retrieveUser " + userName);
//        return Optional.ofNullable(token)
//            .map(String::valueOf)
//            .map(UUID::fromString)
//            .map(tok -> main.subscriptions.get(tok).getLogin())
//            .map(userService::loadUserByUsername)
//            .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token = " + token));
//    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.isAssignableFrom(CustomToken.class);
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        System.out.println("authenticate! " + authentication);
        return authentication;
    }

    public static Collection<? extends GrantedAuthority> getAuthorities(Set<String> roles) {
        return roles.stream()
            .map(SimpleGrantedAuthority::new).toList();
    }
}