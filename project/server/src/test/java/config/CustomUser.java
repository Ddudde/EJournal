package config;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomSecurityContext.class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface CustomUser {
    String username() default "nm12";
    String password() default "1111";
    String[] roles() default { "4" };
}
