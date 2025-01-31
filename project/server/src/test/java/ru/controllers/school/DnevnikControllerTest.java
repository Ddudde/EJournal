package ru.controllers.school;

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
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
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
import static utils.TestUtils.getSub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({DnevnikControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DnevnikControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final TestUtils testUtils = new TestUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MainService mainService;

    @Autowired
    private DBService dbService;

    @Autowired
    private DnevnikController dnevnikController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(dnevnikController)
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
            .tag("DnevnikController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("DnevnikController/" + methodName, resource(snip.build()));
    }

    private final String getDnevnik_Summary = "Отправляет данные о расписании, оценках, домашних заданиях";

    @Test @Tag("getDnevnik")
    @CustomAuth
    void getDnevnik_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getDnevnik_Summary, "getDnevnik_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и с домашками */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID_onlyHomework() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        final School school = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(school.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(school);
        user.getSelecRole().setGrp(group);
        prepareLessons();
        prepareActualPeriod(school);
        prepareHomework();

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"homework\":\"Стр. 62-63 пересказ\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"homework\":\"Упр. 5Стр. 103\"},\"i\":0},\"10.06.22\":{\"0\":{\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"homework\":\"Упр. 6Стр. 103\"},\"i\":0},\"11.06.22\":{\"0\":{\"homework\":\"Упр. 7Стр. 103\"},\"i\":0}}}}"))
            .andDo(defaultSwaggerDocs(getDnevnik_Summary, "getDnevnik_whenGood_KID_onlyHomework"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и оценок, вместе с домашками */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID_withHomework() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        final School school = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(school.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(school);
        user.getSelecRole().setGrp(group);
        prepareLessons();
        prepareActualPeriod(school);
        prepareMarks();
        prepareHomework();

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Стр. 62-63 пересказ\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"mark\":\"Н\",\"weight\":1,\"homework\":\"Упр. 5Стр. 103\"},\"i\":0},\"10.06.22\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 6Стр. 103\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0},\"11.06.22\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 7Стр. 103\"},\"i\":0}}}}"))
            .andDo(defaultSwaggerDocs(getDnevnik_Summary, "getDnevnik_whenGood_KID_withHomework"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и оценок, но без домашек */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        final School school = mock(School.class);
        when(group.getId()).thenReturn(20L);
        when(school.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(school);
        user.getSelecRole().setGrp(group);
        prepareLessons();
        prepareActualPeriod(school);
        prepareMarks();

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"mark\":\"Н\",\"weight\":1},\"i\":0},\"10.06.22\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0},\"11.06.22\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}}}}"))
            .andDo(defaultSwaggerDocs(getDnevnik_Summary, "getDnevnik_whenGood_KID"));
    }

    /** RU: создаём обычные случайные домашки */
    private void prepareHomework() {
        final List<Object[]> homeworks = List.of(
            new Object[]{"Англ. Яз", "10.06.22", "Упр. 5Стр. 103,Упр. 2Стр. 104"},
            new Object[]{"Химия", "10.06.22", "Стр. 62-63 пересказ"},
            new Object[]{"Математика", "10.06.22", "Упр. 6Стр. 103"},
            new Object[]{"Математика", "11.06.22", "Упр. 7Стр. 103"},
            new Object[]{"Англ. Яз", "12.06.22", "Упр. 5Стр. 103"}
        );
        when(dayRepository
            .uniqNameSubAndDatAndHomeworkByParams(eq(20L), eq(20L))).thenReturn(homeworks);
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

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(lessonRepository.findBySchoolIdAndGrpId(20L, 20L)).thenReturn(testUtils.lessons);
    }

    private final String getInfo_Summary = "[start] запускает клиента в раздел дневника и подтверждает клиенту права";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getId()).thenReturn(20L);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/dnevnik/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class DnevnikControllerConfig {

    @Bean
    public DayRepository dayRepository() {
        return mock(DayRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public LessonRepository lessonRepository() {
        return mock(LessonRepository.class, Answers.RETURNS_DEEP_STUBS);
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
    public DnevnikController dnevnikController(MainService mainService, DayRepository dayRepository, DBService dbService) {
        return spy(new DnevnikController(dayRepository, dbService, mainService));
    }
}