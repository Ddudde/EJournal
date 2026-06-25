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
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getAuth;
import static utils.TestUtils.usersTest;

public class HTeachersControllerTest extends AbstractTestIntegration {
    private final IDBService dbService;
    private final RoleRepository roleRepository;
    private final ISSEService sseService;
    private static final String remGroup_Summary = "Удаляет группу + Server Sent Events";
    private static final String addGroup_Summary = "Создаёт группу + Server Sent Events";
    private static final String chGroup_Summary = "Изменяет название группы + Server Sent Events";
    private static final String chPep_Summary = "Изменяет фамилию пользователя + Server Sent Events";
    private static final String remPep_Summary = "Удаляет у пользователя роль завуча + Server Sent Events";
    private static final String addPep_Summary = "Создаёт пользователя-завуча + Server Sent Events";
    private static final String chSch_Summary = "Изменение имени учебного центра администратором портала";
    private static final String addSch_Summary = "Добавление учебного центра администратором портала";
    private static final String remSch_Summary = "Удаление учебного центра администратором портала";
    private static final String getInfo_Summary = "[start] отправка список завучей учебного центра";
    private static final String getInfoForAdmins_Summary = "[start] отправка список завучей учебного центра для администраторов";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    public HTeachersControllerTest(IDBService dbService, RoleRepository roleRepository, ISSEService sseService, HTeachersController hTeachersController) {
        this.dbService = dbService;
        this.roleRepository = roleRepository;
        this.sseService = sseService;
        this.testController = hTeachersController;
        nameTestedClass = "HTeachersController";
    }

    @Test @Tag("remGroup")
    @CustomAuth
    void remGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("remGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void remGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        final User user = dbService.userById(getAuth().getUserId());
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "grId": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenGood_Hteacher"));

        verify(sseService).sendEventFor(any(), eq("remGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addGroup")
    @CustomAuth
    void addGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("addGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void addGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isCreated();
        final School school = mock(School.class);
        final User user = dbService.userById(getAuth().getUserId());
        user.getRoles().get(Roles.HTEACHER).setYO(school);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "31В"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenGood_Hteacher"));

        verify(sseService).sendEventFor(any(), eq("addGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"name\":\"31В\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chGroup")
    @CustomAuth
    void chGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("chGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void chGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        final User user = dbService.userById(getAuth().getUserId());
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "grId": 20,
                "name": "31В"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenGood_Hteacher"));

        verify(sseService).sendEventFor(any(), eq("chGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20,\"name\":\"31В\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);
        
        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser
    void chPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getAuth().getUserId());
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                "name": "Дрыздов А.А."
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"id1\":20,\"name\":\"Дрыздов А.А.\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);
        
        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser
    void remPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getAuth().getUserId());
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"id1\":20}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
    }

    @Test @Tag("addPep")
    @CustomUser
    void addPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isCreated();
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.schoolById(any()).getId()).thenReturn(20L);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "yo": 20,
                "name": "Дрыздов А.А."
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        verify(sseService).sendEventFor(any(), eq("addInfoL2C"), any(), any(), any(), any(), any(), any());
        assertEquals("{\"id1\":20,\"body\":{\"name\":\"Дрыздов А.А.\"}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chSch")
    @CustomAuth
    void chSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenEmpty_Anonim"));
    }

    @Test @Tag("chSch")
    @CustomUser
    void chSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);
        when(school.getId()).thenReturn(20L);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "schId": 20,
                "name": "Гимназия ? 4"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20,\"name\":\"Гимназия ? 4\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addSch")
    @CustomAuth
    void addSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenEmpty_Anonim"));
    }

    @Test @Tag("addSch")
    @CustomUser
    void addSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isCreated();
        
        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Гимназия ? 4"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"name\":\"Гимназия ? 4\"}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("remSch")
    @CustomAuth
    void remSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenEmpty_Anonim"));
    }

    @Test @Tag("remSch")
    @CustomUser
    void remSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "schId": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenGood_Admin"));

        verify(sseService).sendEventFor(any(), eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfo_whenGood_HTEACHER() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getAuth().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_HTEACHER"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomAuth
    void getInfoForAdmins_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomUser
    void getInfoForAdmins_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School sch1 = mock(School.class);
        final School sch2 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        when(sch2.getHteachers()).thenReturn(usersTest);
        when(dbService.getSchools()).thenReturn(List.of(sch1, sch2));

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"0\":{\"pep\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}}}"))
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenGood_Admin"));
    }
}