package config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.security.CustomProvider;
import ru.mirea.security.CustomToken;

import java.util.Set;
import java.util.UUID;

public class CustomSecurityContext implements WithSecurityContextFactory<CustomUser> {
    @Override
    public SecurityContext createSecurityContext(CustomUser customUser) {
        SecurityContext context = SecurityContextHolder.getContext();
//        User user = MockUserUtils.getMockUser(customUser.username());
        Subscriber sub = new Subscriber(customUser.username());
        CustomToken auth = new CustomToken(customUser.username(), customUser.password(), CustomProvider.getAuthorities(Set.of(customUser.roles())), sub, UUID.randomUUID().toString());
        context.setAuthentication(auth);
        return context;
    }
}