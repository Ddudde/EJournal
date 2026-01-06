package config;

import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.controllers.AuthController;
import ru.controllers.ContactController;
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
import ru.controllers.school.analytics.PeriodController;
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
import ru.services.IEmailService;
import ru.services.IPushService;
import ru.services.PushService;
import ru.services.data.GroupService;
import ru.services.data.IGroupService;
import ru.services.data.IUserService;
import ru.services.data.UserService;
import ru.services.db.DBService;
import ru.services.db.IDBService;
import ru.services.db.InitDBService;
import ru.services.logic.*;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.SSE.SSEService;
import ru.services.logic.main.IProfileService;
import ru.services.logic.main.ISettingsService;
import ru.services.logic.main.ProfileService;
import ru.services.logic.main.SettingsService;
import ru.services.logic.people.*;
import ru.services.logic.school.*;
import ru.services.logic.school.analytics.*;

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

    private final IEmailService emailService = mock(EmailService.class);
    private final IPushService pushService = mock(PushService.class, Answers.RETURNS_DEEP_STUBS);
    private final IDBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    private final PasswordEncoder passwordEncoder = spy(new BCryptPasswordEncoder(8));
    private final IGroupService groupService = spy(new GroupService(dbService));
    private final IPeriodService periodService = spy(new PeriodService(periodRepository, schoolRepository));
    private final InitDBService initDBService = spy(new InitDBService(passwordEncoder, settingUserRepository,
        roleRepository, userRepository, schoolRepository, dbService, newsRepository, contactsRepository, systRepository,
        dayRepository, lessonRepository, markRepository, groupRepository, periodRepository, requestRepository, periodService));
    private final IUserService userService = spy(new UserService(dbService, userRepository, settingUserRepository));
    private final IProfileService profileService = spy(new ProfileService(pushService, settingUserRepository,
        userService));
    private final ISSEService sseService = spy(new SSEService());
    private final ISettingsService settingsService = spy(new SettingsService(settingUserRepository, emailService,
        pushService, passwordEncoder, userRepository));
    private final INewsService newsService = spy(new NewsService(systRepository, newsRepository, schoolRepository, pushService));
    private final ITeacherService teacherService = spy(new TeacherService(groupRepository, userRepository, roleRepository,
        schoolRepository, lessonRepository, userService));
    private final IScheduleService scheduleService = spy(new ScheduleService(schoolRepository, lessonRepository,
        userRepository, teacherService, dbService, groupService));
    private final IDnevnikService dnevnikService = spy(new DnevnikService(dayRepository, dbService, scheduleService, periodService));
    private final IAdminsService adminsService = spy(new AdminsService(userRepository, systRepository, roleRepository));
    private final IHTeachersService hTeachersService = spy(new HTeachersService(schoolRepository, groupRepository,
        dbService, userRepository, roleRepository, userService));
    private final IParentsService parentsService = spy(new ParentsService(userRepository, groupRepository, dbService,
        roleRepository, userService));
    private final IStudentsService studentsService = spy(new StudentsService(userRepository, groupRepository,
        roleRepository, dbService));
    private final IKidJournalService kidJournalService = spy(new KidJournalService(markRepository, lessonRepository,
        dayRepository, periodService));
    private final ITestService testService = spy(new TestService(initDBService));
    private final IContactService contactService = spy(new ContactService(contactsRepository, dbService));
    private final IAuthService authService = spy(new AuthService(pushService, settingUserRepository, passwordEncoder,
        dbService, userRepository));
    private final ITeacherJournalService teacherJournalService = spy(new TeacherJournalService(dayRepository,
        markRepository, dbService, lessonRepository, groupService, scheduleService, periodService));
    private final IRequestService requestService = spy(new RequestService(requestRepository));

    @Autowired
    public BeanConfig(GenericApplicationContext context) {
        context.registerBean(DayRepository.class, () -> dayRepository);
        context.registerBean(LessonRepository.class, () -> lessonRepository);
        context.registerBean(NewsRepository.class, () -> newsRepository);
        context.registerBean(MarkRepository.class, () -> markRepository);
        context.registerBean(SystRepository.class, () -> systRepository);
        context.registerBean(RoleRepository.class, () -> roleRepository);
        context.registerBean(SchoolRepository.class, () -> schoolRepository);
        context.registerBean(SettingUserRepository.class, () -> settingUserRepository);

        context.registerBean(IEmailService.class, () -> emailService);
        context.registerBean(IPushService.class, () -> pushService);
        context.registerBean("dbService", IDBService.class, () -> dbService);
        context.registerBean(PasswordEncoder.class, () -> passwordEncoder);
        context.registerBean(InitDBService.class, () -> initDBService);
        context.registerBean(IProfileService.class, () -> profileService);
        context.registerBean(IUserService.class, () -> userService);
        context.registerBean(ISSEService.class, () -> sseService);
        context.registerBean(ISettingsService.class, () -> settingsService);
        context.registerBean(INewsService.class, () -> newsService);
        context.registerBean(IDnevnikService.class, () -> dnevnikService);
        context.registerBean(IAdminsService.class, () -> adminsService);
        context.registerBean(IHTeachersService.class, () -> hTeachersService);
        context.registerBean(IParentsService.class, () -> parentsService);
        context.registerBean(IGroupService.class, () -> groupService);
        context.registerBean(IStudentsService.class, () -> studentsService);
        context.registerBean(ITeacherService.class, () -> teacherService);
        context.registerBean(IKidJournalService.class, () -> kidJournalService);
        context.registerBean(IPeriodService.class, () -> periodService);
        context.registerBean(IScheduleService.class, () -> scheduleService);
        context.registerBean(ITestService.class, () -> testService);
        context.registerBean(IContactService.class, () -> contactService);
        context.registerBean(IAuthService.class, () -> authService);
        context.registerBean(ITeacherJournalService.class, () -> teacherJournalService);
        context.registerBean(IRequestService.class, () -> requestService);

        context.registerBean(TestController.class, () -> spy(new TestController(testService, sseService)));
        context.registerBean(NewsController.class, () -> spy(new NewsController(dbService, newsService, sseService)));
        context.registerBean(ContactController.class, () -> spy(new ContactController(dbService, contactService,
            sseService)));
        context.registerBean(AuthController.class, () -> spy(new AuthController(dbService, authService, sseService)));
        context.registerBean(DnevnikController.class, () -> spy(new DnevnikController(dbService, dnevnikService,
            sseService)));
        context.registerBean(AdminsController.class, () -> spy(new AdminsController(dbService, adminsService,
            sseService, userService)));
        context.registerBean(HTeachersController.class, () -> spy(new HTeachersController(dbService, hTeachersService,
            sseService, userService)));
        context.registerBean(ParentsController.class, () -> spy(new ParentsController(dbService, parentsService,
            groupService, sseService)));
        context.registerBean(StudentsController.class, () -> spy(new StudentsController(dbService, studentsService,
            sseService, userService, groupService)));
        context.registerBean(TeachersController.class, () -> spy(new TeachersController(dbService, teacherService,
            sseService)));
        context.registerBean(ProfileController.class, () -> spy(new ProfileController(dbService, profileService,
            userService, sseService)));
        context.registerBean(SettingsController.class, () -> spy(new SettingsController(dbService, settingsService)));
        context.registerBean(ScheduleController.class, () -> spy(new ScheduleController(dbService, scheduleService,
            sseService)));
        context.registerBean(PeriodController.class, () -> spy(new PeriodController(dbService, periodService,
            sseService)));
        context.registerBean(KidJournalController.class, () -> spy(new KidJournalController(dbService, kidJournalService,
            sseService)));
        context.registerBean(SSEController.class, () -> spy(new SSEController(sseService)));
        context.registerBean(TeacherJournalController.class, () -> spy(new TeacherJournalController(dbService,
            teacherJournalService, sseService)));
        context.registerBean(RequestController.class, () -> spy(new RequestController(dbService, requestService,
            sseService)));
    }
}
