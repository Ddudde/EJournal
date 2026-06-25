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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.security.AuthenticationFilter;
import ru.security.CustomExceptionTranslationFilter;
import ru.security.CustomProvider;
import ru.services.interfaces.IJwtService;
import ru.services.interfaces.db.IDBService;

/** RU: Начало описания security.
 * В БД пароли хранятся зашифрованно(BCryptPasswordEncoder).
 * Аутентификация: JWT-Token в header "x-access-token".
 * Анонимные пользователи тоже наделяются токеном.
 * При аутентификации в системе используются Basic Auth.
 * И хранится токен в клиенте LocalStorage */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@EnableWebSecurity public class SecurityConfig {
    private final CustomProvider provider;
    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
        PathPatternRequestMatcher.withDefaults().matcher("/console_db"),
        PathPatternRequestMatcher.withDefaults().matcher("/auth/refreshToken"),
        PathPatternRequestMatcher.withDefaults().matcher("/console_db/*")
    );
    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);
    public static final String SSE_TOKEN_HEADER = "x-token";
    public static final String ACCESS_TOKEN_HEADER = "x-access-token";
    public static final String NAME_OF_COOKIE = "token";
    private final AuthenticationConfiguration authConfig;
    private final IDBService dbService;
    private final IJwtService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(8);
    private final CustomExceptionTranslationFilter exceptionTranslationFilter = new CustomExceptionTranslationFilter();

    private AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }

    private AuthenticationFilter authenticationFilter() throws Exception {
        return new AuthenticationFilter(PROTECTED_URLS, authConfig.getAuthenticationManager(), bCryptPasswordEncoder, dbService, jwtService);
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
            .addFilterBefore(exceptionTranslationFilter, UsernamePasswordAuthenticationFilter.class)
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