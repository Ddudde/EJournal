package ru.controllers.people;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.school.LessonRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class TeachersControllerTest extends AbstractTestIntegration {
    private final LessonRepository lessonRepository;
    private final RoleRepository roleRepository;
    private final IDBService dbService;
    private final ISSEService sseService;
    private static final String remPep_Summary = "Удаление роли преподавателя";
    private static final String chPep_Summary = "Изменяет ФИО преподавателю учебного центра.";
    private static final String addTea_Summary = "Cоздаёт нового учителя для учебного центра";
    private static final String getTeachers_Summary = "[start] отправка списка учителей учебного центра";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    public TeachersControllerTest(LessonRepository lessonRepository, RoleRepository roleRepository, IDBService dbService, ISSEService sseService, TeachersController teachersController) {
        this.lessonRepository = lessonRepository;
        this.roleRepository = roleRepository;
        this.dbService = dbService;
        this.sseService = sseService;
        this.testController = teachersController;
        nameTestedClass = "TeachersController";
    }
    
    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getAuth().getUserId());
        final Group group = mock(Group.class);
        getSub().setLvlGr("20");
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(new ArrayList<>(usersTest));

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(any(), eq("remPepC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getAuth().getUserId());
        when(dbService.userById(20L)).thenReturn(user);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якуш А.О."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(any(), eq("chPepC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"name\":\"Якуш А.О.\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addTea")
    @CustomAuth
    void addTea_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/teachers/addTea")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addTea_Summary, "addTea_whenEmpty_Anonim"));
    }

    @Test @Tag("addTea")
    @CustomUser(roles = Roles.HTEACHER)
    void addTea_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        getSub().setLvlSch("20");
        when(dbService.schoolById(20L)).thenReturn(sch1);

        mockMvc.perform(post("/teachers/addTea")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якушева А.О."
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addTea_Summary, "addTea_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(any(), eq("addTeaC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"name\":\"Якушева А.О.\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("getTeachers")
    @CustomAuth
    void getTeachers_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getTeachers_Summary, "getTeachers_whenEmpty_Anonim"));
    }

    @Test @Tag("getTeachers")
    @CustomUser(roles = Roles.HTEACHER)
    void getTeachers_whenGood_HTEACHER() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getAuth().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getId()).thenReturn(20L);
        when(sch1.getTeachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);
        prepareTeachersByLessons();

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().string("{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}},\"body\":{\"0\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"}},\"name\":\"Англ. Яз\"},\"1\":{\"tea\":{\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"name\":\"Математика\"}}}"))
            .andDo(defaultSwaggerDocs(getTeachers_Summary, "getTeachers_whenGood_HTEACHER"));
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