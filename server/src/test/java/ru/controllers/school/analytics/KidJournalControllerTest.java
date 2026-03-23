package ru.controllers.school.analytics;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class KidJournalControllerTest extends AbstractTestIntegration {
    private final MarkRepository markRepository;
    private final LessonRepository lessonRepository;
    private final DayRepository dayRepository;
    private final IDBService dbService;
    private final IPeriodService periodService;
    private static final String getInfoPers_Summary = "Отправляет данные о итоговых оценках";
    private static final String getInfo_Summary = "[start] отправляет данные о оценках";

    @Autowired
    public KidJournalControllerTest(MarkRepository markRepository, LessonRepository lessonRepository, DayRepository dayRepository, IDBService dbService, IPeriodService periodService, KidJournalController kidJournalController) {
        this.markRepository = markRepository;
        this.lessonRepository = lessonRepository;
        this.dayRepository = dayRepository;
        this.dbService = dbService;
        this.periodService = periodService;
        this.testController = kidJournalController;
        nameTestedClass = "KidJournalController";
    }

    @Test @Tag("getInfoPers")
    @CustomAuth
    void getInfoPers_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfoPers")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyPers\":{\"352\":\"I четверть\",\"3872\":\"II четверть\",\"3456\":\"IV четверть\",\"9764\":\"III четверть\"},\"bodyM\":{\"Химия\":{\"9764\":\"4\"},\"Англ. Яз\":{\"352\":\"Н\",\"9764\":\"1\"},\"Математика\":{\"352\":\"5\",\"3872\":\"5\",\"9764\":\"2\"}}}"))
            .andDo(defaultSwaggerDocs(getInfoPers_Summary, "getInfoPers_whenGood_KID"));
    }

    /** RU: создаём случайные оценки периодов */
    private void prepareMarksPers() {
        final List<Object[]> marksPers = List.of(
            new Object[]{"Англ. Яз", TEST_UTILS.marksPeriod.get(0)},
            new Object[]{"Математика", TEST_UTILS.marksPeriod.get(1)},
            new Object[]{"Химия", TEST_UTILS.marksPeriod.get(2)},
            new Object[]{"Математика", TEST_UTILS.marksPeriod.get(3)},
            new Object[]{"Математика", TEST_UTILS.marksPeriod.get(4)},
            new Object[]{"Англ. Яз", TEST_UTILS.marksPeriod.get(5)}
        );
        when(markRepository
            .uniqNameSubjectAndMarksByParams(any(), eq("per"), any())).thenReturn(marksPers);
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/journal/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
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
}