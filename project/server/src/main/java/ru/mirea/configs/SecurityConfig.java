package ru.mirea.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.mirea.services.security.AuthenticationFilter;
import ru.mirea.services.security.AuthenticationProvider;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
        new AntPathRequestMatcher("/auth/*"),
        new AntPathRequestMatcher("/news/getNews")
    );

    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    @Autowired
    private AuthenticationProvider provider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }

    @Bean
    AuthenticationFilter authenticationFilter() throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(PROTECTED_URLS);
        filter.setAuthenticationManager(authenticationManager());
        //filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().exceptionHandling()
            .defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_URLS)
            .and().headers().frameOptions().deny()
            .and().csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .anonymous().disable()
            .rememberMe().disable()
//            .authenticationProvider(provider)
            .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests()
            .requestMatchers(PUBLIC_URLS).permitAll()
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
            .roles("0");
    }
}
