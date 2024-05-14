package config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.data.SSE.Subscriber;
import ru.security.CustomToken;

import java.util.UUID;

public class AuthSecurityContext implements WithSecurityContextFactory<CustomAuth> {
    @Override
    public SecurityContext createSecurityContext(CustomAuth customUser) {
        SecurityContext context = SecurityContextHolder.getContext();
        Subscriber sub = new Subscriber();
        CustomToken auth = new CustomToken(sub, UUID.randomUUID().toString());
        context.setAuthentication(auth);
        return context;
    }
}