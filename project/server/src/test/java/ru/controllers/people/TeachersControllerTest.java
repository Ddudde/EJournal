package ru.controllers.people;

import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.controllers.SSE.SSEController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.school.LessonRepository;
import ru.security.user.Roles;
import ru.services.db.DBService;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class TeachersControllerTest extends AbstractTestIntegration {
    private final LessonRepository lessonRepository;
    private final RoleRepository roleRepository;
    private final DBService dbService;
    private static final String remPep_Summary = "Удаление роли преподавателя";
    private static final String chPep_Summary = "Изменяет ФИО преподавателю учебного центра.";
    private static final String addTea_Summary = "Cоздаёт нового учителя для учебного центра";
    private static final String getTeachers_Summary = "[start] отправка списка учителей учебного центра";

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Autowired
    public TeachersControllerTest(LessonRepository lessonRepository, RoleRepository roleRepository, DBService dbService, TeachersController teachersController) {
        this.lessonRepository = lessonRepository;
        this.roleRepository = roleRepository;
        this.dbService = dbService;
        this.testController = teachersController;
        nameTestedClass = "TeachersController";
    }
    
    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        getSub().setLvlGr("20");
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(new ArrayList<>(usersTest));

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764}",
            answer.getValue().toString());
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        when(dbService.userById(20L)).thenReturn(user);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якуш А.О."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764,\"name\":\"Якуш А.О.\"}",
            answer.getValue().toString());
    }

    @Test @Tag("addTea")
    @CustomAuth
    void addTea_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/teachers/addTea")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якушева А.О."
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addTea_Summary, "addTea_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addTeaC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"name\":\"Якушева А.О.\"}",
            answer.getValue().toString());
    }

    @Test @Tag("getTeachers")
    @CustomAuth
    void getTeachers_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getTeachers_Summary, "getTeachers_whenEmpty_Anonim"));
    }

    @Test @Tag("getTeachers")
    @CustomUser(roles = Roles.HTEACHER)
    void getTeachers_whenGood_HTEACHER() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getId()).thenReturn(20L);
        when(sch1.getTeachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);
        prepareTeachersByLessons();

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().string("{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}}"))
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