package ru.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
import ru.services.db.DBService;
import ru.services.db.InitDBService;
import ru.services.logic.school.analytics.IPeriodService;

/** RU: конфигурация Бинов с настройкой init-методов */
@Configuration
public class SpringConfig {

    @Bean(initMethod = "postConstruct")
    public InitDBService iniDBService(PasswordEncoder passwordEncoder, DBService dbService,
                                      SettingUserRepository settingUserRepository, RoleRepository roleRepository, UserRepository userRepository,
                                      SchoolRepository schoolRepository, NewsRepository newsRepository, ContactsRepository contactsRepository,
                                      SystRepository systRepository, DayRepository dayRepository, LessonRepository lessonRepository,
                                      MarkRepository markRepository, GroupRepository groupRepository, PeriodRepository periodRepository,
                                      RequestRepository requestRepository, IPeriodService periodService) {
        return new InitDBService(passwordEncoder, settingUserRepository, roleRepository, userRepository,
            schoolRepository, dbService, newsRepository, contactsRepository, systRepository, dayRepository,
            lessonRepository, markRepository, groupRepository, periodRepository, requestRepository, periodService);
    }
}
