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
import ru.services.MainService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;

/** RU: конфигурация Бинов с настройкой init */
@Configuration
public class SpringConfig {

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService, LessonRepository lessonRepository) {
        return new MainService(dbService, lessonRepository);
    }

    @Bean(initMethod = "postConstruct")
    public IniDBService iniDBService(MainService mainService, PasswordEncoder passwordEncoder, DBService dbService,
         SettingUserRepository settingUserRepository, RoleRepository roleRepository, UserRepository userRepository,
         SchoolRepository schoolRepository, NewsRepository newsRepository, ContactsRepository contactsRepository,
         SystRepository systRepository, DayRepository dayRepository, LessonRepository lessonRepository,
         MarkRepository markRepository, GroupRepository groupRepository, PeriodRepository periodRepository,
         RequestRepository requestRepository) {
        return new IniDBService(passwordEncoder, settingUserRepository, roleRepository, userRepository,
            schoolRepository, dbService, newsRepository, contactsRepository, systRepository, dayRepository,
            lessonRepository, markRepository, groupRepository, periodRepository, requestRepository, mainService);
    }
}
