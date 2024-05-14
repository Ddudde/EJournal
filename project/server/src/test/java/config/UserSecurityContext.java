package config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.ObjectUtils;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.security.CustomProvider;
import ru.security.CustomToken;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static ru.Main.datas;
import static utils.RandomUtils.usersTest;

public class UserSecurityContext implements WithSecurityContextFactory<CustomUser> {
    @Override
    public SecurityContext createSecurityContext(CustomUser customUser) {
        SecurityContext context = SecurityContextHolder.getContext();
        User user = spy(usersTest.get(5));
        user.setUsername(customUser.username());
        user.setPassword(customUser.password());
        if(!ObjectUtils.isEmpty(customUser.roles())) {
            Long roleOfCustom = Long.parseLong(customUser.roles()[0]);
            user.getRoles().put(roleOfCustom, user.getSelecRole());
            user.setSelRole(roleOfCustom);
        }
        when(datas.getDbService().userByLogin(customUser.username())).thenReturn(user);
        Subscriber sub = new Subscriber(customUser.username());
        CustomToken auth = new CustomToken(customUser.username(), customUser.password(), CustomProvider.getAuthorities(Set.of(customUser.roles())), sub, UUID.randomUUID().toString());
        context.setAuthentication(auth);
        return context;
    }
}