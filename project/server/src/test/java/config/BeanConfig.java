package config;

import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.controllers.AuthController;
import ru.controllers.ContactsController;
import ru.controllers.NewsController;
import ru.controllers.SSE.SSEController;
import ru.controllers.TestController;
import ru.controllers.main.ProfileController;
import ru.controllers.main.SettingsController;
import ru.controllers.people.*;
import ru.controllers.school.DnevnikController;
import ru.controllers.school.RequestController;
import ru.controllers.school.TeacherJournalController;
import ru.controllers.school.analytics.KidJournalController;
import ru.controllers.school.analytics.PeriodsController;
import ru.controllers.school.analytics.ScheduleController;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
import ru.security.CustomAccessDenied;
import ru.services.EmailService;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class BeanConfig {
    private final SettingUserRepository settingUserRepository = mock(SettingUserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class, Answers.RETURNS_DEEP_STUBS);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final NewsRepository newsRepository = mock(NewsRepository.class);
    private final ContactsRepository contactsRepository = mock(ContactsRepository.class);
    private final DayRepository dayRepository = mock(DayRepository.class, Answers.RETURNS_DEEP_STUBS);
    private final MarkRepository markRepository = mock(MarkRepository.class, Answers.RETURNS_DEEP_STUBS);
    private final LessonRepository lessonRepository = mock(LessonRepository.class, Answers.RETURNS_DEEP_STUBS);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final PeriodRepository periodRepository = mock(PeriodRepository.class);
    private final SchoolRepository schoolRepository = mock(SchoolRepository.class);
    private final SystRepository systRepository = mock(SystRepository.class);
    private final RequestRepository requestRepository = mock(RequestRepository.class);

    private final EmailService emailService = mock(EmailService.class);
    private final PushService pushService = mock(PushService.class, Answers.RETURNS_DEEP_STUBS);
    private final DBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    private final MainService mainService = spy(new MainService(dbService, lessonRepository));
    private final PasswordEncoder passwordEncoder = spy(new BCryptPasswordEncoder(8));
    private final IniDBService iniDBService = spy(new IniDBService(passwordEncoder, settingUserRepository, roleRepository, userRepository,
        schoolRepository, dbService, newsRepository, contactsRepository, systRepository, dayRepository,
        lessonRepository, markRepository, groupRepository, periodRepository, requestRepository, mainService));

    @Autowired
    public BeanConfig(GenericApplicationContext context) {
        context.registerBean(DayRepository.class, () -> dayRepository);
        context.registerBean(LessonRepository.class, () -> lessonRepository);
        context.registerBean(NewsRepository.class, () -> newsRepository);
        context.registerBean(MarkRepository.class, () -> markRepository);
        context.registerBean(SystRepository.class, () -> systRepository);
        context.registerBean(RoleRepository.class, () -> roleRepository);
        context.registerBean(SchoolRepository.class, () -> schoolRepository);

        context.registerBean(EmailService.class, () -> emailService);
        context.registerBean(PushService.class, () -> pushService);
        context.registerBean("dbService", DBService.class, () -> dbService);
        context.registerBean(MainService.class, () -> mainService, this::defaultInitMethodMainService);
        context.registerBean(PasswordEncoder.class, () -> passwordEncoder);
        context.registerBean(IniDBService.class, () -> iniDBService);

        context.registerBean(TestController.class, () -> spy(new TestController(iniDBService, mainService)));
        context.registerBean(NewsController.class, () -> spy(new NewsController(dbService, pushService, systRepository,
            newsRepository, schoolRepository, mainService)));
        context.registerBean(ContactsController.class, () -> spy(new ContactsController(dbService, mainService,
            contactsRepository)));
        context.registerBean(AuthController.class, () -> spy(new AuthController(passwordEncoder, settingUserRepository,
            dbService, mainService, pushService, userRepository)));
        context.registerBean(DnevnikController.class, () -> spy(new DnevnikController(dayRepository, dbService,
            mainService)));
        context.registerBean(AdminsController.class, () -> spy(new AdminsController(userRepository, systRepository,
            dbService, mainService, roleRepository)));
        context.registerBean(HTeachersController.class, () -> spy(new HTeachersController(schoolRepository,
            groupRepository, dbService, mainService, userRepository, roleRepository)));
        context.registerBean(ParentsController.class, () -> spy(new ParentsController(userRepository, dbService,
            groupRepository, mainService, roleRepository)));
        context.registerBean(StudentsController.class, () -> spy(new StudentsController(userRepository, dbService,
            groupRepository, mainService, roleRepository)));
        context.registerBean(TeachersController.class, () -> spy(new TeachersController(userRepository, dbService,
            groupRepository, mainService, roleRepository, schoolRepository)));
        context.registerBean(ProfileController.class, () -> spy(new ProfileController(userRepository, mainService,
            pushService, settingUserRepository, dbService)));
        context.registerBean(SettingsController.class, () -> spy(new SettingsController(passwordEncoder, mainService,
            settingUserRepository, emailService, pushService, userRepository, dbService)));
        context.registerBean(ScheduleController.class, () -> spy(new ScheduleController(mainService, dbService,
            schoolRepository, lessonRepository, userRepository)));
        context.registerBean(PeriodsController.class, () -> spy(new PeriodsController(periodRepository,
            schoolRepository, mainService, dbService)));
        context.registerBean(KidJournalController.class, () -> spy(new KidJournalController(markRepository,
            lessonRepository, dbService, mainService, dayRepository)));
        context.registerBean(SSEController.class, () -> spy(new SSEController()));
        context.registerBean(TeacherJournalController.class, () -> spy(new TeacherJournalController(dayRepository,
            markRepository, dbService, mainService, lessonRepository)));
        context.registerBean(RequestController.class, () -> spy(new RequestController(requestRepository, mainService,
            dbService)));
    }

    public void defaultInitMethodMainService(BeanDefinition bd) {
        bd.setInitMethodName("postConstruct");
    }
}
