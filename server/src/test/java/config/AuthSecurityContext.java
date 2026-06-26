package config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.db.IDBService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** RU: создаёт SecurityContext для SpringTest c @CustomAuth
 * @see CustomAuth */
@RequiredArgsConstructor
public class AuthSecurityContext implements WithSecurityContextFactory<CustomAuth> {
    private final IDBService dbService;

    @Override
    public SecurityContext createSecurityContext(CustomAuth customUser) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final SubscriberDTO sub = new SubscriberDTO();
        final AuthToken auth = new AuthToken(sub, UUID.randomUUID().toString());
        when(dbService.userById(any())).thenReturn(null);
        context.setAuthentication(auth);
        return context;
    }
}