package ru.controllers.school.analytics;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;
import utils.TestUtils;

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
import static utils.TestUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({KidJournalControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class KidJournalControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final TestUtils testUtils = new TestUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private MarkRepository markRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MainService mainService;

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private DBService dbService;

    @Autowired
    private KidJournalController kidJournalController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(kidJournalController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .setCustomArgumentResolvers(subscriberMethodArgumentResolver)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    /** RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     * @param summary Заголовок эндпоинта
     * @param methodName Название теста
     * @return Сниппет */
    private RestDocumentationResultHandler defaultSwaggerDocs(String summary, String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("KidJournalController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("KidJournalController/" + methodName, resource(snip.build()));
    }

    private final String getInfoPers_Summary = "Отправляет данные о итоговых оценках";

    @Test @Tag("getInfoPers")
    @CustomAuth
    void getInfoPers_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfoPers")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoPers_Summary, "getInfoPers_whenEmpty_Anonim"));
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
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyPers\":{\"352\":\"I четверть\",\"3872\":\"II четверть\",\"9764\":\"III четверть\",\"3456\":\"IV четверть\"},\"bodyM\":{\"Химия\":{\"9764\":\"4\"},\"Англ. Яз\":{\"9764\":\"1\",\"352\":\"Н\"},\"Математика\":{\"9764\":\"2\",\"352\":\"5\",\"3872\":\"5\"}}}"))
            .andDo(defaultSwaggerDocs(getInfoPers_Summary, "getInfoPers_whenGood_KID"));
    }

    /** RU: создаём случайные оценки периодов */
    private void prepareMarksPers() {
        final List<Object[]> marksPers = List.of(
            new Object[]{"Англ. Яз", testUtils.marksPeriod.get(0)},
            new Object[]{"Математика", testUtils.marksPeriod.get(1)},
            new Object[]{"Химия", testUtils.marksPeriod.get(2)},
            new Object[]{"Математика", testUtils.marksPeriod.get(3)},
            new Object[]{"Математика", testUtils.marksPeriod.get(4)},
            new Object[]{"Англ. Яз", testUtils.marksPeriod.get(5)}
        );
        when(markRepository
            .uniqNameSubjectAndMarksByParams(any(), eq("per"), any())).thenReturn(marksPers);
    }

    private final String getInfo_Summary = "[start] отправляет данные о оценках";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
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
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"bodyJ\":{\"Химия\":{\"days\":{\"10.06.22\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"}}},\"Англ. Яз\":{\"days\":{\"12.06.22\":{\"mark\":\"Н\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"}}},\"Математика\":{\"days\":{\"10.06.22\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"}}}}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    /** RU: создаём обычные случайные оценки */
    private void prepareListLessons() {
        final List<String> lessons = List.of("Англ. Яз", "Математика", "Химия");
        when(lessonRepository
            .uniqSubNameBySchoolAndGrp(eq(20L), eq(20L))).thenReturn(lessons);
    }

    /** RU: создаём обычные случайные оценки */
    private void prepareMarks() {
        final List<Object[]> marks = List.of(
            new Object[]{"Англ. Яз", "10.06.22", testUtils.marks.get(0)},
            new Object[]{"Математика", "10.06.22", testUtils.marks.get(1)},
            new Object[]{"Химия", "10.06.22", testUtils.marks.get(2)},
            new Object[]{"Математика", "10.06.22", testUtils.marks.get(3)},
            new Object[]{"Математика", "11.06.22", testUtils.marks.get(4)},
            new Object[]{"Англ. Яз", "12.06.22", testUtils.marks.get(5)}
        );
        when(dayRepository
            .uniqNameSubjectAndDatAndMarksByParams(eq(20L), eq(20L), any())).thenReturn(marks);
    }

    /** RU: создаём периоды обучения и выбираем 3тий период */
    private void prepareActualPeriod(School school) {
        when(school.getPeriods()).thenReturn(testUtils.periods);
        doReturn(testUtils.periods.get(2)).when(mainService).getActualPeriodBySchool(any());
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class KidJournalControllerConfig {

    @Bean
    public MarkRepository markRepository() {
        return mock(MarkRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public LessonRepository lessonRepository() {
        return mock(LessonRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public DayRepository dayRepository() {
        return mock(DayRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService, LessonRepository lessonRepository) {
        return spy(new MainService(dbService, lessonRepository));
    }

    @Bean
    public KidJournalController kidJournalController(DayRepository dayRepository, MarkRepository markRepository,
        LessonRepository lessonRepository, DBService dbService, MainService mainService) {
        return spy(new KidJournalController(markRepository, lessonRepository, dbService, mainService, dayRepository));
    }
}