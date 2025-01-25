package config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.data.SSE.Subscriber;
import ru.security.user.CustomToken;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.Main.datas;

/** RU: создаёт SecurityContext для SpringTest c @CustomAuth
 * @see CustomAuth */
public class AuthSecurityContext implements WithSecurityContextFactory<CustomAuth> {
    @Override
    public SecurityContext createSecurityContext(CustomAuth customUser) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Subscriber sub = new Subscriber();
        final CustomToken auth = new CustomToken(sub, UUID.randomUUID().toString());
        when(datas.getDbService().userByLogin(any())).thenReturn(null);
        context.setAuthentication(auth);
        return context;
    }
}