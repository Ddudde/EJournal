package ru.controllers.school;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.SSEController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Day;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Mark;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({TeacherJournalControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TeacherJournalControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final TestUtils testUtils = new TestUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";
    private MockedStatic theMock;

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private MarkRepository markRepository;

    @Autowired
    private DBService dbService;

    @Autowired
    private MainService mainService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TeacherJournalController teacherJournalController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @AfterEach
    void afterEach() {
        theMock.close();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        theMock = Mockito.mockStatic(SSEController.class);
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(teacherJournalController)
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
            .tag("TeacherJournalController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("TeacherJournalController/" + methodName, resource(snip.build()));
    }

    private final String addHomework_Summary = "Создаёт домашнее задание на определённое занятие дня группе";

    @Test @Tag("addHomework")
    @CustomAuth
    void addHomework_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/pjournal/addHomework")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addHomework_Summary, "addHomework_whenEmpty_Anonim"));
    }

    @Test @Tag("addHomework")
    @CustomUser(roles = Roles.TEACHER)
    void addHomework_whenGood_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        final Group group = mock(Group.class);
        user.getSelecRole().setYO(sch1);
        getSub().setLvlMore2("Химия");
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(post("/pjournal/addHomework")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "group": 20,
                "day": "10.06.22",
                "homework": "Упр. 6Стр. 103"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addHomework_Summary, "addHomework_whenGood_TEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addHomeworkC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"day\":\"10.06.22\",\"homework\":\"Упр. 6Стр. 103\"}",
            answer.getValue().toString());
    }

    private final String addMark_Summary = "Создаёт оценку к определённому уроку либо целому периоду(итоговая оценка)";

    @Test @Tag("addMark")
    @CustomAuth
    void addMark_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/pjournal/addMark")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addMark_Summary, "addMark_whenEmpty_Anonim"));
    }

    @Test @Tag("addMark")
    @CustomUser(roles = Roles.TEACHER)
    void addMark_whenPeriodMark_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        final Group group = mock(Group.class);
        user.getSelecRole().setYO(sch1);
        getSub().setLvlMore2("Химия");
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);
        prepareMarkPeriod();

        mockMvc.perform(post("/pjournal/addMark")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "kid": 20,
                "group": 20,
                "per": 20,
                "day": "10.06.22",
                "mark": "5",
                "weight": 1,
                "style": "Ответ на уроке"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addMark_Summary, "addMark_whenPeriodMark_TEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"kid\":20,\"day\":\"10.06.22\",\"body\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Химия\",\"per\":20}}",
            answer.getValue().toString());
    }

    private void prepareMarkPeriod() {
        when(markRepository
            .findByTypeAndStyleAndPeriodIdAndUsrId("per", "Химия", 20L, 20L)).thenReturn(null);
    }

    @Test @Tag("addMark")
    @CustomUser(roles = Roles.TEACHER)
    void addMark_whenExistMark_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        final Group group = mock(Group.class);
        user.getSelecRole().setYO(sch1);
        getSub().setLvlMore2("Химия");
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);
        prepareListMarksId();
        prepareDaysForExistMark();
        prepareMarksForExistMark();

        mockMvc.perform(post("/pjournal/addMark")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "kid": 20,
                "group": 20,
                "day": "10.06.22,1",
                "mark": "5",
                "weight": 1,
                "style": "Ответ на уроке"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addMark_Summary, "addMark_whenExistMark_TEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"kid\":20,\"day\":\"10.06.22,1\",\"body\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\",\"per\":null}}",
            answer.getValue().toString());
    }

    private void prepareMarksForExistMark() {
        final List<Mark> marks = List.of(new Mark(), new Mark(), new Mark());
        when(markRepository.findByIdInAndUsrId(any(), eq(20L))).thenReturn(marks);
    }

    private void prepareDaysForExistMark() {
        final List<Day> days = List.of(mock(Day.class), mock(Day.class), mock(Day.class));
        when(dayRepository
            .findBySchoolIdAndTeacherIdAndGrpIdAndNameSubject(20L, 20L, 20L, "Химия")).thenReturn(days);
    }

    @Test @Tag("addMark")
    @CustomUser(roles = Roles.TEACHER)
    void addMark_whenGood_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        final Group group = mock(Group.class);
        user.getSelecRole().setYO(sch1);
        getSub().setLvlMore2("Химия");
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(post("/pjournal/addMark")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "kid": 20,
                "group": 20,
                "day": "10.06.22",
                "mark": "5",
                "weight": 1,
                "style": "Ответ на уроке"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addMark_Summary, "addMark_whenGood_TEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"kid\":20,\"day\":\"10.06.22\",\"body\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\",\"per\":null}}",
            answer.getValue().toString());
    }

    private final String getInfoPart3_Summary = "Отправляет данные о оценках, домашних заданиях и итоговых оценках группы подчинённой преподавателю на дисциплине";

    @Test @Tag("getInfoPart3")
    @CustomAuth
    void getInfoPart3_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP3/{groupId}", 20L)
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoPart3_Summary, "getInfoPart3_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoPart3")
    @CustomUser(roles = Roles.TEACHER)
    void getInfoPart3_whenGood_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        final Group group = mock(Group.class);
        user.getSelecRole().setYO(sch1);
        getSub().setLvlMore2("Химия");
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);
        when(group.getKids()).thenReturn(usersTest);
        prepareHomework();
        preparePeriods(sch1);
        prepareListMarksId();
        prepareMarksByKid();

        mockMvc.perform(get("/pjournal/getInfoP3/{groupId}", 20L)
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyD\":{\"12.06.22\":\"Упр. 5Стр. 103\",\"10.06.22\":\"Упр. 6Стр. 103\",\"11.06.22\":\"Упр. 7Стр. 103\"},\"bodyK\":{\"3872\":{\"name\":\"Якушева А.О.\",\"days\":{\"12.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,4\":{\"mark\":\"Н\",\"weight\":1}},\"avg\":{\"9764\":\"4\",\"352\":\"Н\",\"3872\":\"5\"}},\"1705\":{\"name\":\"Дроздов А.А.\",\"days\":{\"12.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"12.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"10.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,1\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,2\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,3\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"11.06.22,4\":{\"mark\":\"Н\",\"weight\":1}},\"avg\":{\"9764\":\"4\",\"352\":\"Н\",\"3872\":\"5\"}},\"1840\":{\"name\":\"Пестов Л.А.\",\"days\":{},\"avg\":{}},\"3225\":{\"name\":\"Никифорова Н.А.\",\"days\":{},\"avg\":{}},\"9764\":{\"name\":\"Силин А.К.\",\"days\":{},\"avg\":{}}}}"))
            .andDo(defaultSwaggerDocs(getInfoPart3_Summary, "getInfoPart3_whenGood_TEACHER"));
    }

    /** RU: создаёт обычные оценки и за период, нескольким ученикам */
    private void prepareMarksByKid() {
        when(markRepository.findByIdInAndUsrIdAndPeriodId(any(), eq(3872L), eq(9764L))).thenReturn(testUtils.marks);
        when(markRepository
            .findByIdInAndUsrIdAndPeriodId(any(), eq(1705L), eq(9764L))).thenReturn(testUtils.marks);
        when(markRepository
            .findByPeriodInAndTypeAndStyleAndUsrId(testUtils.periods, "per", "Химия", 3872L)).thenReturn(testUtils.marksPeriod);
        when(markRepository
            .findByPeriodInAndTypeAndStyleAndUsrId(testUtils.periods, "per", "Химия", 1705L)).thenReturn(testUtils.marksPeriod);
    }

    private void prepareListMarksId() {
        final List<Object[]> marksByDay = List.of(
            new Object[]{"10.06.22", 10L},
            new Object[]{"10.06.22", 12L},
            new Object[]{"10.06.22", 13L},
            new Object[]{"11.06.22", 14L},
            new Object[]{"12.06.22", 15L}
        );
        when(dayRepository
            .uniqDatAndMarksByParams(20L, 20L, 20L, "Химия")).thenReturn(marksByDay);
    }

    /** RU: создаём обычные случайные домашки */
    private void prepareHomework() {
        final List<Object[]> homeworks = List.of(
            new Object[]{"10.06.22", "Упр. 6Стр. 103"},
            new Object[]{"11.06.22", "Упр. 7Стр. 103"},
            new Object[]{"12.06.22", "Упр. 5Стр. 103"}
        );
        when(dayRepository
            .uniqDatAndHomeworkByParams(20L, 20L, "Химия")).thenReturn(homeworks);
    }

    private final String getInfoPart2_Summary = "[start] отправляет данные о группах учебного центра подчинённые преподавателю на дисциплине";

    @Test @Tag("getInfoPart2")
    @CustomAuth
    void getInfoPart2_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP2/{nameSubject}", "Math")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoPart2_Summary, "getInfoPart2_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoPart2")
    @CustomUser(roles = Roles.TEACHER)
    void getInfoPart2_whenGood_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        user.getSelecRole().setYO(sch1);
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        prepareGroup();

        mockMvc.perform(get("/pjournal/getInfoP2/{nameSubject}", "Math")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyG\":{\"21\":\"1А\",\"22\":\"1Б\",\"23\":\"1В\"},\"firstG\":21}"))
            .andDo(defaultSwaggerDocs(getInfoPart2_Summary, "getInfoPart2_whenGood_TEACHER"));
    }

    private void prepareGroup() {
        final List<Long> groupsL = List.of(21L, 22L, 23L);
        when(lessonRepository
            .uniqGroupsBySchoolAndSubNameAndTeacher(20L, "Math", 20L)).thenReturn(groupsL);
        when(dbService.groupById(21L)).thenReturn(groups.get(0));
        when(dbService.groupById(22L)).thenReturn(groups.get(1));
        when(dbService.groupById(23L)).thenReturn(groups.get(2));
    }

    private final String getInfoPart1_Summary = "[start] отправляет данные о расписании, периодах обучения и дисциплинах преподавателя";

    @Test @Tag("getInfoPart1")
    @CustomAuth
    void getInfoPart1_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP1")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoPart1_Summary, "getInfoPart1_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoPart1")
    @CustomUser(roles = Roles.TEACHER)
    void getInfoPart1_whenGood_TEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        user.getSelecRole().setYO(sch1);
        when(user.getId()).thenReturn(20L);
        when(sch1.getId()).thenReturn(20L);
        prepareUniqSubjectsName();
        preparePeriods(sch1);
        prepareLessons();

        mockMvc.perform(get("/pjournal/getInfoP1")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyPred\":{\"0\":\"Англ.Яз.\",\"1\":\"Химия\",\"2\":\"Физика\"},\"bodyPers\":{\"352\":\"I четверть\",\"3872\":\"II четверть\",\"9764\":\"III четверть\",\"3456\":\"IV четверть\"},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyS\":{}}"))
            .andDo(defaultSwaggerDocs(getInfoPart1_Summary, "getInfoPart1_whenGood_TEACHER"));
    }

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(lessonRepository
            .findBySchoolIdAndTeacherId(20L, 20L)).thenReturn(testUtils.lessons);
    }

    /** RU: создаём периоды обучения и выбираем 3тий период */
    private void preparePeriods(School school) {
        when(school.getPeriods()).thenReturn(testUtils.periods);
        doReturn(testUtils.periods.get(2)).when(mainService).getActualPeriodBySchool(any());
    }

    private void prepareUniqSubjectsName() {
        final List<String> subjs = List.of("Англ.Яз.", "Химия", "Физика");
        when(lessonRepository
            .uniqSubNameBySchoolAndTeacher(20L, 20L)).thenReturn(subjs);
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class TeacherJournalControllerConfig {

    @Bean
    public DayRepository dayRepository() {
        return mock(DayRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public MarkRepository markRepository() {
        return mock(MarkRepository.class, Answers.RETURNS_DEEP_STUBS);
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
    public TeacherJournalController teacherJournalController(LessonRepository lessonRepository,
         DayRepository dayRepository, MarkRepository markRepository, DBService dbService, MainService mainService) {
        return spy(new TeacherJournalController(dayRepository, markRepository, dbService, mainService, lessonRepository));
    }
}