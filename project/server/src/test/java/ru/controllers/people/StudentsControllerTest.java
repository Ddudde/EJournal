package ru.controllers.people;

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
import ru.data.reps.auth.RoleRepository;
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

public class StudentsControllerTest extends AbstractTestIntegration {
    private final RoleRepository roleRepository;
    private final DBService dbService;
    private static final String remPep_Summary = "Удаляет роль ученика у пользователя + Server Sent Events";
    private static final String chPep_Summary = "Изменяет ФИО ученика + Server Sent Events";
    private static final String addPep_Summary = "Создаёт пользователя-ученика и отправляет информацию + Server Sent Events";
    private static final String getStud_Summary = "Отправляет список учеников группы";
    private static final String getInfo_Summary = "[start] запускает клиента в раздел Одноклассники и подтверждает клиенту права";
    private static final String getInfoForHTeacher_Summary = "[start] отправляет список групп учебного центра и подтверждает клиенту права";

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Autowired
    public StudentsControllerTest(RoleRepository roleRepository, DBService dbService, StudentsController studentsController) {
        this.roleRepository = roleRepository;
        this.dbService = dbService;
        this.testController = studentsController;
        nameTestedClass = "StudentsController";
    }

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/students/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final List<User> users = new ArrayList<>(usersTest);
        final Group group = mock(Group.class);
        getSub().setLvlGr(20L+"");
        when(dbService.userById(20L)).thenReturn(users.get(0));
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(users);

        mockMvc.perform(delete("/students/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":3872}",
            answer.getValue().toString());
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/students/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        final User user = getCloneUsers(usersTest.get(0));
        when(dbService.userById(20L)).thenReturn(user);

        mockMvc.perform(patch("/students/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                "name": "Вейс А.А."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":3872,\"name\":\"Вейс А.А.\"}",
            answer.getValue().toString());
    }

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/students/addPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
    }

    @Test @Tag("addPep")
    @CustomUser(roles = Roles.HTEACHER)
    void addPep_whenGood_HTEACHER() throws Exception {
        final School school = mock(School.class);
        final Group group = mock(Group.class);
        getSub().setLvlGr(20L+"");
        getSub().setLvlSch(20L+"");
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.groupById(20L)).thenReturn(group);
        when(dbService.schoolById(20L)).thenReturn(school);


        mockMvc.perform(post("/students/addPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Вейс А.А."
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_HTEACHER"));

        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"name\":\"Вейс А.А.\"}}",
            answer.getValue().toString());
    }

    @Test @Tag("getStud")
    @CustomAuth
    void getStud_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/students/getStud/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getStud_Summary, "getStud_whenEmpty_Anonim"));
    }

    @Test @Tag("getStud")
    @CustomUser(roles = Roles.KID)
    void getStud_whenGood_KID() throws Exception {
        final School sch = mock(School.class);
        final Group group = TEST_UTILS.groups.get(0);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch);
        when(dbService.getFirstRole(any()).getGrp().getId()).thenReturn(20L);
        when(dbService.groupById(20L)).thenReturn(group);
        when(sch.getGroups()).thenReturn(TEST_UTILS.groups);

        mockMvc.perform(get("/students/getStud/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getStud_Summary, "getStud_whenGood_KID"));
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/students/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        mockMvc.perform(get("/students/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    @Test @Tag("getInfoForHTeacher")
    @CustomAuth
    void getInfoForHTeacher_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/students/getInfoFH")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForHTeacher")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfoForHTeacher_whenGood_HTEACHER() throws Exception {
        final School sch = mock(School.class);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch);
        when(sch.getGroups()).thenReturn(TEST_UTILS.groups);

        mockMvc.perform(get("/students/getInfoFH")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"bodyG\":{\"2323\":\"1А\",\"3456\":\"1Б\",\"4354\":\"1В\"},\"firstG\":2323}"))
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenGood_HTEACHER"));
    }
}