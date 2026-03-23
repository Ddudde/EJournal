package ru.controllers.school;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getSub;

public class DnevnikControllerTest extends AbstractTestIntegration {
    private final DayRepository dayRepository;
    private final LessonRepository lessonRepository;
    private final IDBService dbService;
    private final IPeriodService periodService;
    private static final String getDnevnik_Summary = "Отправляет данные о расписании, оценках, домашних заданиях";
    private static final String getInfo_Summary = "[start] запускает клиента в раздел дневника и подтверждает клиенту права";

    @Autowired
    public DnevnikControllerTest(DayRepository dayRepository, LessonRepository lessonRepository, IDBService dbService, IPeriodService periodService, DnevnikController dnevnikController) {
        this.dayRepository = dayRepository;
        this.lessonRepository = lessonRepository;
        this.dbService = dbService;
        this.periodService = periodService;
        this.testController = dnevnikController;
        nameTestedClass = "DnevnikController";
    }

    @Test @Tag("getDnevnik")
    @CustomAuth
    void getDnevnik_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getDnevnik")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"id\":9764,\"name\":\"Силин А.К.\"}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"marks\":{\"0\":{\"homework\":\"Стр. 62-63 пересказ\"}},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"marks\":{\"0\":{\"homework\":\"Упр. 5Стр. 103\"}},\"i\":0},\"10.06.22\":{\"marks\":{\"0\":{\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"}},\"i\":0}},\"Математика\":{\"10.06.22\":{\"marks\":{\"0\":{\"homework\":\"Упр. 6Стр. 103\"}},\"i\":0},\"11.06.22\":{\"marks\":{\"0\":{\"homework\":\"Упр. 7Стр. 103\"}},\"i\":0}}}}"))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"id\":9764,\"name\":\"Силин А.К.\"}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Стр. 62-63 пересказ\"}},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"marks\":{\"0\":{\"mark\":\"Н\",\"weight\":1,\"homework\":\"Упр. 5Стр. 103\"}},\"i\":0},\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 5Стр. 103,Упр. 2Стр. 104\"}},\"i\":0}},\"Математика\":{\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 6Стр. 103\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"}},\"i\":0},\"11.06.22\":{\"marks\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\",\"homework\":\"Упр. 7Стр. 103\"}},\"i\":0}}}}"))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"id\":9764,\"name\":\"Силин А.К.\"}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}}},\"min\":\"12.01.24\",\"max\":\"29.03.24\",\"bodyD\":{\"Химия\":{\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"4\",\"weight\":1,\"type\":\"Ответ на уроке\"}},\"i\":0}},\"Англ. Яз\":{\"12.06.22\":{\"marks\":{\"0\":{\"mark\":\"Н\",\"weight\":1}},\"i\":0},\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"1\",\"weight\":1,\"type\":\"Ответ на уроке\"}},\"i\":0}},\"Математика\":{\"10.06.22\":{\"marks\":{\"0\":{\"mark\":\"2\",\"weight\":1,\"type\":\"Ответ на уроке\"},\"1\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"}},\"i\":0},\"11.06.22\":{\"marks\":{\"0\":{\"mark\":\"5\",\"weight\":1,\"type\":\"Ответ на уроке\"}},\"i\":0}}}}"))
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
            new Object[]{"Англ. Яз", "10.06.22", TEST_UTILS.marks.get(0)},
            new Object[]{"Математика", "10.06.22", TEST_UTILS.marks.get(1)},
            new Object[]{"Химия", "10.06.22", TEST_UTILS.marks.get(2)},
            new Object[]{"Математика", "10.06.22", TEST_UTILS.marks.get(3)},
            new Object[]{"Математика", "11.06.22", TEST_UTILS.marks.get(4)},
            new Object[]{"Англ. Яз", "12.06.22", TEST_UTILS.marks.get(5)}
        );
        when(dayRepository
            .uniqNameSubjectAndDatAndMarksByParams(eq(20L), eq(20L), any())).thenReturn(marks);
    }

    /** RU: создаём периоды обучения и выбираем 3тий период */
    private void prepareActualPeriod(School school) {
        when(school.getPeriods()).thenReturn(TEST_UTILS.periods);
        doReturn(TEST_UTILS.periods.get(2)).when(periodService).getActualPeriodBySchool(any());
    }

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(lessonRepository.findBySchoolIdAndGrpId(20L, 20L)).thenReturn(TEST_UTILS.lessons);
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/dnevnik/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

}