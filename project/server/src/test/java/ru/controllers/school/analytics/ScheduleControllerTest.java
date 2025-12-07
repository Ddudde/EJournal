package ru.controllers.school.analytics;

import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.controllers.SSE.SSEController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.school.LessonRepository;
import ru.security.user.Roles;
import ru.services.db.DBService;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class ScheduleControllerTest extends AbstractTestIntegration {
    private final LessonRepository lessonRepository;
    private final DBService dbService;
    private static final String addLesson_Summary = "Добавление урока + Server Sent Events";
    private static final String getSchedule_Summary = "Отправляет данные о расписании для группы";
    private static final String getInfo_Summary = "[start] подтверждает клиенту права";
    private static final String getInfoForHTeacherOrTEACHER_Summary = "[start] отправляет список групп и учителей учебного центра";

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Autowired
    public ScheduleControllerTest(LessonRepository lessonRepository, DBService dbService, ScheduleController scheduleController) {
        this.lessonRepository = lessonRepository;
        this.dbService = dbService;
        this.testController = scheduleController;
        nameTestedClass = "ScheduleController";
    }

    @Test @Tag("addLesson")
    @CustomAuth
    void addLesson_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/schedule/addLesson")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addLesson_Summary, "addLesson_whenEmpty_Anonim"));
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addLesson_Summary, "addLesson_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addLessonC"), answer.capture(), any(), any(), any(), any(), any()),
            times(3));
        assertEquals("{\"bodyT\":{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}},\"day\":1,\"les\":1,\"body\":{\"name\":\"Химия\",\"cabinet\":\"504Б\",\"prepod\":{\"name\":\"Дрыздов А.А.\",\"id\":21},\"group\":null}}",
            answer.getValue().toString());
    }

    @Test @Tag("getSchedule")
    @CustomAuth
    void getSchedule_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getSchedule/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getSchedule_Summary, "getSchedule_whenEmpty_Anonim"));
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}}}"))
            .andDo(defaultSwaggerDocs(getSchedule_Summary, "getSchedule_whenGood_HTEACHER"));
    }

    /** RU: создаём уроки для учеников */
    private void prepareLessons() {
        when(lessonRepository
            .findBySchoolIdAndGrpId(20L, 20L)).thenReturn(TEST_UTILS.lessons);
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/schedule/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    @Test @Tag("getInfoForHTeacherOrTEACHER")
    @CustomAuth
    void getInfoForHTeacherOrTEACHER_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/schedule/getInfoToHT")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoForHTeacherOrTEACHER_Summary, "getInfoForHTeacherOrTEACHER_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForHTeacherOrTEACHER")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfoForHTeacherOrTEACHER_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        prepareTeachersByLessons();
        when(sch1.getGroups()).thenReturn(TEST_UTILS.groups);
        when(sch1.getTeachers()).thenReturn(usersTest);
        when(sch1.getId()).thenReturn(20L);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);

        mockMvc.perform(get("/schedule/getInfoToHT")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyG\":{\"2323\":\"1А\",\"3456\":\"1Б\",\"4354\":\"1В\"},\"firstG\":2323,\"bodyT\":{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}}}"))
            .andDo(defaultSwaggerDocs(getInfoForHTeacherOrTEACHER_Summary, "getInfoForHTeacherOrTEACHER_whenGood_HTEACHER"));
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
        when(lessonRepository.uniqTeachersLBySchool(20L)).thenReturn(lessons);
    }
}