package ru.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.services.EmailService;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;

/** RU: конфигурация Бинов с настройкой init */
@Configuration
public class SpringConfig {

    @Bean(initMethod = "postConstruct")
    public MainService mainService(PushService pushService, DBService dbService, EmailService emailService) {
        return new MainService(pushService, dbService, emailService);
    }

    @Bean(initMethod = "postConstruct")
    public IniDBService iniDBService(MainService mainService, PasswordEncoder passwordEncoder) {
        return new IniDBService(passwordEncoder);
    }
}
