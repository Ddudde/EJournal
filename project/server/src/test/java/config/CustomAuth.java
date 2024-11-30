package config;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** RU: аноним */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = AuthSecurityContext.class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface CustomAuth {
    String username() default "anonymousUser";
    String password() default "";
    String[] roles() default { "ANONYMOUS" };
}
