package config;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;
import ru.configs.AppConfig;
import ru.security.user.Roles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** RU: замена @WithMockUser, по умолчанию админ */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = UserSecurityContext.class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface CustomUser {
    String username() default AppConfig.TEST_LOGIN;
    String password() default "1111";
    Roles[] roles() default { Roles.ADMIN };
}
