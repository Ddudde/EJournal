package config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.ObjectUtils;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.CustomToken;
import ru.services.db.DBService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static utils.TestUtils.getCloneUsers;
import static utils.TestUtils.usersTest;

/** RU: создаёт SecurityContext для SpringTest c @CustomUser
 * @see CustomUser */
@RequiredArgsConstructor
public class UserSecurityContext implements WithSecurityContextFactory<CustomUser> {
    private final DBService dbService;

    @Override
    public SecurityContext createSecurityContext(CustomUser customUser) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final User user = spy(getCloneUsers(usersTest.get(5)));
        if(!ObjectUtils.isEmpty(customUser.roles())) {
            user.getRoles().put(customUser.roles()[0], user.getSelecRole());
            user.setSelRole(customUser.roles()[0]);
        }
        final SubscriberDTO sub = new SubscriberDTO(customUser.username());
        sub.setUserId(user.getId());
        final CustomToken auth = new CustomToken(customUser.password(), user.getAuthorities(), sub, UUID.randomUUID().toString());
        user.setUsername(customUser.username());
        user.setPassword(customUser.password());

        when(dbService.userByLogin(customUser.username())).thenReturn(user);
        when(dbService.userById(user.getId())).thenReturn(user);
        when(dbService.existUserBySubscription(any())).thenReturn(true);
        context.setAuthentication(auth);
        return context;
    }
}