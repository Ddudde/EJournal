package ru.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.SchoolRepository;
import ru.services.db.DBService;
import ru.services.db.InitDBService;
import ru.services.db.RandomizeService;

/** RU: конфигурация Бинов с настройкой init-методов */
@Configuration
public class SpringConfig {

    @Bean(initMethod = "postConstruct")
    public InitDBService iniDBService(PasswordEncoder passwordEncoder, DBService dbService,
          SettingUserRepository settingUserRepository, RoleRepository roleRepository, UserRepository userRepository,
          SchoolRepository schoolRepository, RandomizeService randomizeService) {
        return new InitDBService(passwordEncoder, settingUserRepository, roleRepository, userRepository,
            schoolRepository, dbService, randomizeService);
    }
}
