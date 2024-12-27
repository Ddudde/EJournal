package ru.controllers.school.analytics;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;
import utils.RandomUtils;

import javax.servlet.ServletException;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;
import static utils.RandomUtils.usersTest;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({ScheduleControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ScheduleControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private ScheduleController scheduleController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final RandomUtils randomUtils = new RandomUtils();
    private static final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @BeforeAll
    static void beforeAll() throws ServletException {
        authInjector.afterPropertiesSet();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    private RestDocumentationResultHandler default_Docs(String summary, String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("ScheduleController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ScheduleController/" + methodName, resource(snip.build()));
    }

    private final String addLesson_Summary = "Добавление урока + Server Sent Events";

    @Test @Tag("addLesson")
    @CustomAuth
    void addLesson_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/schedule/addLesson")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(addLesson_Summary, "addLesson_whenEmpty_Anonim"));
    }

    @Test @Tag("addLesson")
    @CustomUser(roles = Roles.HTEACHER)
    void addLesson_whenGood_HTEACHER() throws Exception {
        final Group group = mock(Group.class);
        final School sch1 = mock(School.class);
        getSub().setLvlSch(20L + "");
        when(sch1.getTeachers()).thenReturn(usersTest);
        when(group.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.groupById(20L)).thenReturn(group);
        when(dbService.schoolById(20L)).thenReturn(sch1);
        prepareTeachersByLessons();

        mockMvc.perform(post("/schedule/addLesson")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "group": 20,
                "day": 1,
                "obj": {
                   "name": "Химия",
                   "cabinet": "504Б",
                   "prepod": {
                       "name": "Дрыздов А.А.",
                       "id": 21
                   }
               }
            }
            """))
            .andExpect(status().isCreated())
            .andDo(default_Docs(addLesson_Summary, "addLesson_whenGood_HTEACHER"));

        verify(authController, times(3)).sendEventFor(eq("addLessonC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"bodyT\":{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}},\"day\":1,\"les\":1,\"body\":{\"name\":\"Химия\",\"cabinet\":\"504Б\",\"prepod\":{\"name\":\"Дрыздов А.А.\",\"id\":21},\"group\":null}}",
            answer.getValue().toString());
    }

    private final String getSchedule_Summary = "Отправляет данные о расписании для группы";

    @Test @Tag("getSchedule")
    @CustomAuth
    void getSchedule_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getSchedule/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getSchedule_Summary, "getSchedule_whenEmpty_Anonim"));
    }

    @Test @Tag("getSchedule")
    @CustomUser(roles = Roles.HTEACHER)
    void getSchedule_whenGood_HTEACHER() throws Exception {
        final Group group = mock(Group.class);
        final School sch1 = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.groupById(20L)).thenReturn(group);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);
        prepareLessons();

        mockMvc.perform(get("/schedule/getSchedule/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}}}"))
            .andDo(default_Docs(getSchedule_Summary, "getSchedule_whenGood_HTEACHER"));
    }

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(dbService.getLessonRepository()
            .findBySchoolIdAndGrpId(20L, 20L)).thenReturn(randomUtils.lessons);
    }

    private final String getInfo_Summary = "[start] подтверждает клиенту права";
    private final String getInfoForHTeacherOrTEACHER_Summary = "[start] отправляет список групп и учителей учебного центра";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/schedule/getInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    @Test @Tag("getInfoForHTeacherOrTEACHER")
    @CustomAuth
    void getInfoForHTeacherOrTEACHER_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getInfoToHT")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getInfoForHTeacherOrTEACHER_Summary, "getInfoForHTeacherOrTEACHER_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForHTeacherOrTEACHER")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfoForHTeacherOrTEACHER_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        prepareTeachersByLessons();
        when(sch1.getGroups()).thenReturn(randomUtils.groups);
        when(sch1.getTeachers()).thenReturn(usersTest);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);

        mockMvc.perform(get("/schedule/getInfoToHT")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyG\":{\"2323\":\"1А\",\"3456\":\"1Б\",\"4354\":\"1В\"},\"firstG\":2323,\"bodyT\":{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}}}"))
            .andDo(default_Docs(getInfoForHTeacherOrTEACHER_Summary, "getInfoForHTeacherOrTEACHER_whenGood_HTEACHER"));
    }

    /** RU: создаём данные для списка дисциплина-учителя */
    private void prepareTeachersByLessons() {
        final List<Object[]> lessons = List.of(
            new Object[]{"Англ. Яз", 22L},
            new Object[]{"Англ. Яз", 23L},
            new Object[]{"Англ. Яз", 24L},
            new Object[]{"Математика", 25L}
        );
        when(dbService.userById(22L)).thenReturn(usersTest.get(0));
        when(dbService.userById(23L)).thenReturn(usersTest.get(1));
        when(dbService.userById(24L)).thenReturn(usersTest.get(2));
        when(dbService.userById(25L)).thenReturn(usersTest.get(3));
        when(dbService.getLessonRepository()
            .uniqTeachersLBySchool(20L)).thenReturn(lessons);
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class ScheduleControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public ScheduleController scheduleController(AuthController authController) {
        return spy(new ScheduleController(authController));
    }
}