package ru.mirea.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.ServerService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private UserService userService;

    public final Map<UUID, Subscriber> subscriptions = new ConcurrentHashMap<>();

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
        System.out.println("additionalAuthenticationChecks " + userDetails);
    }

    @Override
    protected User retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        Object token = usernamePasswordAuthenticationToken.getCredentials();
        System.out.println("retrieveUser " + userName);
        return Optional.ofNullable(token)
            .map(String::valueOf)
            .map(UUID::fromString)
            .map(tok -> subscriptions.get(tok).getLogin())
            .map(userService::loadUserByUsername)
            .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token = " + token));
    }
}