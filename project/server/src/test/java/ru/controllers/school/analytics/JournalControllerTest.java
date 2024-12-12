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
import ru.data.models.school.Group;
import ru.data.models.school.Mark;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.Main.datas;
import static utils.RandomUtils.defaultDescription;
import static utils.RandomUtils.usersTest;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({JournalControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JournalControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private JournalController journalController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(journalController)
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
            .tag("JournalController")
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("JournalController/" + methodName, resource(snip.build()));
    }

    private final String getInfoPers_Summary = "Отправляет данные о итоговых оценках";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("getInfoPers")
    @CustomAuth
    void getInfoPers_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfoPers")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getInfoPers_Summary, "getInfoPers_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * отправляет JSON'ом информацию о итоговых оценках */
    @Test @Tag("getInfoPers")
    @CustomUser(roles = Roles.KID)
    void getInfoPers_whenGood_KID() throws Exception {
        final Group group = mock(Group.class);
        final School school = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(school.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(school);
        when(dbService.getFirstRole(any()).getGrp()).thenReturn(group);
        prepareActualPeriod(school);
        prepareMarksPers();
        prepareListLessons();

        mockMvc.perform(get("/journal/getInfoPers")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"bodyPers\":{\"352\":\"I четверть\",\"3872\":\"II четверть\",\"9764\":\"III четверть\",\"3456\":\"IV четверть\"},\"bodyM\":{\"Химия\":{\"9764\":\"4\"},\"Англ. Яз\":{\"9764\":\"1\",\"352\":\"Н\"},\"Математика\":{\"9764\":\"2\",\"352\":\"5\",\"3872\":\"5\"}}}"))
            .andDo(default_Docs(getInfoPers_Summary, "getInfoPers_whenGood_KID"));
    }

    /** RU: создаём случайные оценки периодов */
    private void prepareMarksPers() {
        final List<Object[]> marksPers = List.of(
            new Object[]{"Англ. Яз", new Mark(usersTest.get(1), randomUtils.periods.get(2), "1", 1, "per", null)},
            new Object[]{"Математика", new Mark(usersTest.get(1), randomUtils.periods.get(2), "2", 1, "per", null)},
            new Object[]{"Химия", new Mark(usersTest.get(1), randomUtils.periods.get(2), "4", 1, "per", null)},
            new Object[]{"Математика", new Mark(usersTest.get(1), randomUtils.periods.get(0), "5", 1, "per", null)},
            new Object[]{"Математика", new Mark(usersTest.get(1), randomUtils.periods.get(1), "5", 1, "per", null)},
            new Object[]{"Англ. Яз", new Mark(usersTest.get(1), randomUtils.periods.get(0), "Н", 1, "per", null)}
        );
        when(dbService.getMarkRepository()
            .uniqNameSubjectAndMarksByParams(any(), eq("per"), any())).thenReturn(marksPers);
    }

    private final String getInfo_Summary = "[start] отправляет данные о оценках";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * отправляет JSON'ом информацию о оценках */
    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        final Group group = mock(Group.class);
        final School school = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(school.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(school);
        when(dbService.getFirstRole(any()).getGrp()).thenReturn(group);
        prepareActualPeriod(school);
        prepareMarks();
        prepareListLessons();

        mockMvc.perform(get("/journal/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"bodyJ\":{\"Химия\":{\"days\":{\"10.06.22\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"}}},\"Англ. Яз\":{\"days\":{\"12.06.22\":{\"mark\":\"Н\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"}}},\"Математика\":{\"days\":{\"10.06.22\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"}}}}}"))
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    /** RU: создаём обычные случайные оценки */
    private void prepareListLessons() {
        final List<String> lessons = List.of("Англ. Яз", "Математика", "Химия");
        when(dbService.getLessonRepository()
            .uniqSubNameBySchoolAndGrp(eq(20L), eq(20L))).thenReturn(lessons);
    }

    /** RU: создаём обычные случайные оценки */
    private void prepareMarks() {
        final List<Object[]> marks = List.of(
            new Object[]{"Англ. Яз", "10.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "1", 1, "norm", "Ответ на уроке")},
            new Object[]{"Математика", "10.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "2", 1, "norm", "Ответ на уроке")},
            new Object[]{"Химия", "10.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "4", 1, "norm", "Ответ на уроке")},
            new Object[]{"Математика", "10.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "5", 1, "norm", "Ответ на уроке")},
            new Object[]{"Математика", "11.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "5", 1, "norm", "Ответ на уроке")},
            new Object[]{"Англ. Яз", "12.06.22", new Mark(usersTest.get(1), randomUtils.periods.get(2), "Н", 1, "norm", null)}
        );
        when(dbService.getDayRepository()
            .uniqNameSubjectAndDatAndMarksByParams(eq(20L), eq(20L), any())).thenReturn(marks);
    }

    /** RU: создаём периоды обучения и выбираем 3тий период */
    private void prepareActualPeriod(School school) {
        when(school.getPeriods()).thenReturn(randomUtils.periods);
        doReturn(randomUtils.periods.get(2)).when(datas).getActualPeriodBySchool(any());
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class JournalControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return spy(new MainService(null, dbService, null));
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public JournalController journalController(AuthController authController) {
        return spy(new JournalController(authController));
    }
}