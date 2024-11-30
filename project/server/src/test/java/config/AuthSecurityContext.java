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

//toDo: обязательно прописать javaDoc!!!!!!!
public class AuthSecurityContext implements WithSecurityContextFactory<CustomAuth> {
    @Override
    public SecurityContext createSecurityContext(CustomAuth customUser) {
        SecurityContext context = SecurityContextHolder.getContext();
        Subscriber sub = new Subscriber();
        CustomToken auth = new CustomToken(sub, UUID.randomUUID().toString());
        when(datas.getDbService().userByLogin(any())).thenReturn(null);
        context.setAuthentication(auth);
        return context;
    }
}