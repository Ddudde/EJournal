package ru.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.security.AuthenticationFilter;
import ru.security.CustomProvider;
import ru.services.db.IDBService;

/** RU: Начало описания security.
 * В БД пароли хранятся зашифрованно(BCryptPasswordEncoder).
 * Авторизация Token(UUID) в header "x-access-token".
 * Анонимные пользователи тоже наделяются токеном.
 * При авторизации в системе используются логин/пароль.
 * Они передаются без шифрования в POST auth/auth
 * И хранится токен в клиенте LocalStorage */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@EnableWebSecurity public class SecurityConfig {
    private final CustomProvider provider;
    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
        new AntPathRequestMatcher("/console_db"),
        new AntPathRequestMatcher("/console_db/*")
    );
    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);
    public static final String authTokenHeader = "x-access-token";
    private final AuthenticationConfiguration authConfig;
    private final IDBService dbService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(8);

    private AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }

    private AuthenticationFilter authenticationFilter() throws Exception {
        return new AuthenticationFilter(PROTECTED_URLS, authConfig.getAuthenticationManager(), bCryptPasswordEncoder, dbService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(provider)
            .cors(Customizer.withDefaults())
            .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(config -> config.defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_URLS))
            .headers(config -> config.frameOptions(FrameOptionsConfig::disable))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)// В AuthenticationFilter функционал
            .logout(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return bCryptPasswordEncoder;
    }
}