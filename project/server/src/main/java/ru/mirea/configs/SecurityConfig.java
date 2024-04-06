package ru.mirea.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.mirea.security.AuthenticationFilter;
import ru.mirea.security.CustomProvider;
import ru.mirea.security.UserService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomProvider provider;

    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
        new AntPathRequestMatcher("/swagger-resources"),
        new AntPathRequestMatcher("/swagger-resources/*"),
        new AntPathRequestMatcher("/swagger-ui"),
        new AntPathRequestMatcher("/swagger-ui/*"),
        new AntPathRequestMatcher("/v3/*"),
        new AntPathRequestMatcher("/console_db"),
        new AntPathRequestMatcher("/console_db/*")
    );

    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(provider);
        auth.userDetailsService(userService);
    }

    private AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }

    private AuthenticationFilter authenticationFilter() throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(PROTECTED_URLS);
        filter.setAuthenticationManager(authenticationManager());
//        filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().exceptionHandling()
            .defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_URLS)
            .and().headers().frameOptions().disable()
            .and().csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
//            .anonymous().disable()
            .rememberMe().disable()
//            .authenticationProvider(provider)
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
            .requestMatchers(PUBLIC_URLS).permitAll()
//            .requestMatchers(PUBLIC_URLS).access("isAuthenticated() OR isAnonymous()")
//            .requestMatchers(PUBLIC_URLS).hasAnyRole("ANONYMOUS", "0", "1", "2", "3", "4")
//            .requestMatchers(PROTECTED_URLS).hasRole("0")
            .anyRequest().authenticated();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("foo")
            .password("foo")
            .roles("0")
            .and()
            .withUser("admin")
            .password("admin")
            .roles("4")
            .and()
            .withUser("nm12")
            .password("1111")
            .roles("0")
            .and()
            .withUser("anonymousUser")
            .password("")
            .roles("ANONYMOUS");
    }
}
