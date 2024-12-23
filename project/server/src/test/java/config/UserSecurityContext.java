package config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.ObjectUtils;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.security.user.CustomToken;

import java.util.UUID;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static ru.Main.datas;
import static utils.RandomUtils.getCloneUsers;
import static utils.RandomUtils.usersTest;

/** RU: создаёт SecurityContext для SpringTest c @CustomUser
 * @see CustomUser */
public class UserSecurityContext implements WithSecurityContextFactory<CustomUser> {
    @Override
    public SecurityContext createSecurityContext(CustomUser customUser) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final User user = spy(getCloneUsers(usersTest.get(5)));
        final Subscriber sub = new Subscriber(customUser.username());
        final CustomToken auth = new CustomToken(customUser.password(), user.getAuthorities(), sub, UUID.randomUUID().toString());
        user.setUsername(customUser.username());
        user.setPassword(customUser.password());

        if(!ObjectUtils.isEmpty(customUser.roles())) {
            user.getRoles().put(customUser.roles()[0], user.getSelecRole());
            user.setSelRole(customUser.roles()[0]);
        }

        when(datas.getDbService().userByLogin(customUser.username())).thenReturn(user);
        context.setAuthentication(auth);
        return context;
    }
}