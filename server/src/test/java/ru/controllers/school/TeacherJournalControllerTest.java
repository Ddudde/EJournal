package ru.controllers.school;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Day;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Mark;
import ru.data.DAO.school.School;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class TeacherJournalControllerTest extends AbstractTestIntegration {
    private final DayRepository dayRepository;
    private final MarkRepository markRepository;
    private final IDBService dbService;
    private final LessonRepository lessonRepository;
    private final ISSEService sseService;
    private final IPeriodService periodService;
    private static final String addHomework_Summary = "Создаёт домашнее задание на определённое занятие дня группе";
    private static final String addMark_Summary = "Создаёт оценку к определённому уроку либо целому периоду(итоговая оценка)";
    private static final String getInfoPart3_Summary = "Отправляет данные о оценках, домашних заданиях и итоговых оценках группы подчинённой преподавателю на дисциплине";
    private static final String getInfoPart2_Summary = "[start] отправляет данные о группах учебного центра подчинённые преподавателю на дисциплине";
    private static final String getInfoPart1_Summary = "[start] отправляет данные о расписании, периодах обучения и дисциплинах преподавателя";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    public TeacherJournalControllerTest(DayRepository dayRepository, MarkRepository markRepository, IDBService dbService, LessonRepository lessonRepository, ISSEService sseService, IPeriodService periodService, TeacherJournalController teacherJournalController) {
        this.dayRepository = dayRepository;
        this.markRepository = markRepository;
        this.dbService = dbService;
        this.lessonRepository = lessonRepository;
        this.sseService = sseService;
        this.periodService = periodService;
        this.testController = teacherJournalController;
        nameTestedClass = "TeacherJournalController";
    }

    @Test @Tag("addHomework")
    @CustomAuth
    void addHomework_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/pjournal/addHomework/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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

        mockMvc.perform(post("/pjournal/addHomework/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "group": 20,
                "day": "10.06.22",
                "homework": "Упр. 6Стр. 103"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addHomework_Summary, "addHomework_whenGood_TEACHER"));

        verify(sseService).sendEventFor(eq("addHomeworkC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"homework\":\"Упр. 6Стр. 103\",\"day\":\"10.06.22\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addMark")
    @CustomAuth
    void addMark_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/pjournal/addMark/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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

        mockMvc.perform(post("/pjournal/addMark/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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

        verify(sseService).sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"day\":\"10.06.22\",\"kid\":20,\"body\":{\"mark\":\"5\",\"type\":\"Химия\",\"weight\":1,\"per\":20}}",
            gson.toJson(answer.getValue()));
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

        mockMvc.perform(post("/pjournal/addMark/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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

        verify(sseService).sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"day\":\"10.06.22,1\",\"kid\":20,\"body\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1}}",
            gson.toJson(answer.getValue()));
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

        mockMvc.perform(post("/pjournal/addMark/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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

        verify(sseService).sendEventFor(eq("addMarkC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"day\":\"10.06.22\",\"kid\":20,\"body\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("getInfoPart3")
    @CustomAuth
    void getInfoPart3_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP3/{groupId}/", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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

        mockMvc.perform(get("/pjournal/getInfoP3/{groupId}/", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyD\":{\"12.06.22\":\"Упр. 5Стр. 103\",\"10.06.22\":\"Упр. 6Стр. 103\",\"11.06.22\":\"Упр. 7Стр. 103\"},\"bodyK\":{\"3872\":{\"name\":\"Якушева А.О.\",\"days\":{\"10.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"12.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1}},\"avg\":{\"352\":\"Н\",\"3872\":\"5\",\"9764\":\"4\"}},\"1840\":{\"name\":\"Пестов Л.А.\",\"days\":{},\"avg\":{}},\"9764\":{\"name\":\"Силин А.К.\",\"days\":{},\"avg\":{}},\"1705\":{\"name\":\"Дроздов А.А.\",\"days\":{\"10.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"12.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,3\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,4\":{\"mark\":\"Н\",\"weight\":1},\"11.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,1\":{\"mark\":\"4\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,2\":{\"mark\":\"5\",\"type\":\"Ответ на уроке\",\"weight\":1},\"12.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22,0\":{\"mark\":\"2\",\"type\":\"Ответ на уроке\",\"weight\":1},\"10.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1},\"11.06.22\":{\"mark\":\"1\",\"type\":\"Ответ на уроке\",\"weight\":1}},\"avg\":{\"352\":\"Н\",\"3872\":\"5\",\"9764\":\"4\"}},\"3225\":{\"name\":\"Никифорова Н.А.\",\"days\":{},\"avg\":{}}}}"))
            .andDo(defaultSwaggerDocs(getInfoPart3_Summary, "getInfoPart3_whenGood_TEACHER"));
    }

    /** RU: создаёт обычные оценки и за период, нескольким ученикам */
    private void prepareMarksByKid() {
        when(markRepository.findByIdInAndUsrIdAndPeriodId(any(), eq(3872L), eq(9764L))).thenReturn(TEST_UTILS.marks);
        when(markRepository
            .findByIdInAndUsrIdAndPeriodId(any(), eq(1705L), eq(9764L))).thenReturn(TEST_UTILS.marks);
        when(markRepository
            .findByPeriodInAndTypeAndStyleAndUsrId(TEST_UTILS.periods, "per", "Химия", 3872L)).thenReturn(TEST_UTILS.marksPeriod);
        when(markRepository
            .findByPeriodInAndTypeAndStyleAndUsrId(TEST_UTILS.periods, "per", "Химия", 1705L)).thenReturn(TEST_UTILS.marksPeriod);
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

    @Test @Tag("getInfoPart2")
    @CustomAuth
    void getInfoPart2_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP2/{nameSubject}/", "Math")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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

        mockMvc.perform(get("/pjournal/getInfoP2/{nameSubject}/", "Math")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"firstG\":21,\"bodyG\":{\"21\":\"1А\",\"22\":\"1Б\",\"23\":\"1В\"}}"))
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

    @Test @Tag("getInfoPart1")
    @CustomAuth
    void getInfoPart1_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/pjournal/getInfoP1/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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

        mockMvc.perform(get("/pjournal/getInfoP1/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyPred\":{\"0\":\"Англ.Яз.\",\"1\":\"Химия\",\"2\":\"Физика\"},\"bodyPers\":{\"352\":\"I четверть\",\"3872\":\"II четверть\",\"3456\":\"IV четверть\",\"9764\":\"III четверть\"},\"bodyS\":{}}"))
            .andDo(defaultSwaggerDocs(getInfoPart1_Summary, "getInfoPart1_whenGood_TEACHER"));
    }

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(lessonRepository
            .findBySchoolIdAndTeacherId(20L, 20L)).thenReturn(TEST_UTILS.lessons);
    }

    /** RU: создаём периоды обучения и выбираем 3тий период */
    private void preparePeriods(School school) {
        when(school.getPeriods()).thenReturn(TEST_UTILS.periods);
        doReturn(TEST_UTILS.periods.get(2)).when(periodService).getActualPeriodBySchool(any());
    }

    private void prepareUniqSubjectsName() {
        final List<String> subjs = List.of("Англ.Яз.", "Химия", "Физика");
        when(lessonRepository
            .uniqSubNameBySchoolAndTeacher(20L, 20L)).thenReturn(subjs);
    }
}