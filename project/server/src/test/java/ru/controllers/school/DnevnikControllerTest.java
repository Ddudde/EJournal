package ru.controllers.school;

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
import ru.data.models.auth.User;
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
@Import({DnevnikControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DnevnikControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private DnevnikController dnevnikController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(dnevnikController)
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
            .tag("DnevnikController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("DnevnikController/" + methodName, resource(snip.build()));
    }

    private final String getDnevnik_Summary = "Отправляет данные о расписании, оценках, домашних заданиях";

    /** RU: аноним
     * отправляет 404 код-ответ */
    @Test @Tag("getDnevnik")
    @CustomAuth
    void getDnevnik_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getDnevnik_Summary, "getDnevnik_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и с домашками */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID_onlyHomework() throws Exception {
        final User user = getSub().getUser();
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
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"homework\":\"Стр. 62-63 пересказ\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"homework\":\"Упр. 5Стр. 103\"},\"i\":0},\"10.06.22\":{\"0\":{\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"homework\":\"Упр. 6Стр. 103\"},\"i\":0},\"11.06.22\":{\"0\":{\"homework\":\"Упр. 7Стр. 103\"},\"i\":0}}}}"))
            .andDo(default_Docs(getDnevnik_Summary, "getDnevnik_whenGood_KID_onlyHomework"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и оценок, вместе с домашками */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID_withHomework() throws Exception {
        final User user = getSub().getUser();
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
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Стр. 62-63 пересказ\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"mark\":\"Н\",\"weight\":1,\"homework\":\"Упр. 5Стр. 103\"},\"i\":0},\"10.06.22\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 6Стр. 103\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0},\"11.06.22\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 7Стр. 103\"},\"i\":0}}}}"))
            .andDo(default_Docs(getDnevnik_Summary, "getDnevnik_whenGood_KID_withHomework"));
    }

    /** RU: ученик
     * имитирует отправку расписаний, периодов и оценок, но без домашек */
    @Test @Tag("getDnevnik")
    @CustomUser(roles = Roles.KID)
    void getDnevnik_whenGood_KID() throws Exception {
        final User user = getSub().getUser();
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
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"0\":{\"mark\":\"Н\",\"weight\":1},\"i\":0},\"10.06.22\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}},\"Математика\":{\"10.06.22\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0},\"11.06.22\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"i\":0}}}}"))
            .andDo(default_Docs(getDnevnik_Summary, "getDnevnik_whenGood_KID"));
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
        when(dbService.getDayRepository()
            .uniqNameSubAndDatAndHomeworkByParams(eq(20L), eq(20L))).thenReturn(homeworks);
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

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(dbService.getLessonRepository()
            .findBySchoolIdAndGrpId(20L, 20L)).thenReturn(randomUtils.lessons);
    }

    private final String getInfo_Summary = "[start] запускает клиента в раздел дневника и подтверждает клиенту права";

    /** RU: аноним
     * отправляет 404 код-ответ */
    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * подтверждает клиенту права */
    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getId()).thenReturn(20L);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/dnevnik/getInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class DnevnikControllerConfig {

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
    public DnevnikController dnevnikController(AuthController authController) {
        return spy(new DnevnikController(authController));
    }
}