package ru.controllers.people;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
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

public class ParentsControllerTest extends AbstractTestIntegration {
    private final RoleRepository roleRepository;
    private final IDBService dbService;
    private final ISSEService sseService;
    private static final String remPep_Summary = "Создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";
    private static final String chPep_Summary = "создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";
    private static final String addPar_Summary = "создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";
    private static final String getParents_Summary = "отправляет список ребёнок-родители группы";
    private static final String getInfo_Summary = "[start] запускает клиента в раздел Родители и подтверждает клиенту права";
    private static final String getInfoForHTeacher_Summary = "[start] отправляет список групп учебного центра и подтверждает клиенту права";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    public ParentsControllerTest(RoleRepository roleRepository, IDBService dbService, ISSEService sseService, ParentsController parentsController) {
        this.roleRepository = roleRepository;
        this.dbService = dbService;
        this.sseService = sseService;
        this.testController = parentsController;
        nameTestedClass = "ParentsController";
    }

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/parents/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * удаляет роль родителя и отправляет JSON'ом инфу */
    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final Group group = mock(Group.class);
        final List<User> users = new ArrayList<>(usersTest);
        when(dbService.userById(20L)).thenReturn(usersTest.get(0));
        getSub().setLvlGr("20");
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(users);

        mockMvc.perform(delete("/parents/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(eq("remPepC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any());
        assertEquals("{\"id\":3872}",
            gson.toJson(answer.getValue()));
        assertEquals(users.size(), 5);
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/parents/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        when(dbService.userById(20L)).thenReturn(getCloneUsers(usersTest.get(0)));

        mockMvc.perform(patch("/parents/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                name : "Петров П.А."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(eq("chPepC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any());
        assertEquals("{\"id\":3872,\"name\":\"Петров П.А.\"}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("addPar")
    @CustomAuth
    void addPar_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/parents/addPar")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPar_Summary, "addPar_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * создаёт родителя(лей) и отправляет JSON'ом инфу */
    @Test @Tag("addPar")
    @CustomUser(roles = Roles.HTEACHER)
    void addPar_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.userById(20L)).thenReturn(getCloneUsers(usersTest.get(0)));
        getSub().setLvlSch("20");
        when(dbService.schoolById(20L)).thenReturn(sch1);

        mockMvc.perform(post("/parents/addPar")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                "bod": {
                    name : "Петров П.А.",
                    par : {
                        "id1": {
                            name : "Петров А.А."
                        },
                        "id2": {
                            name : "Петрова А.Б."
                        }
                    }
                }
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPar_Summary, "addPar_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(eq("addParC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any());
        assertEquals("{\"id\":3872,\"body\":{\"name\":\"Якушева А.О.\",\"par\":{\"null\":{\"name\":\"Петрова А.Б.\"}},\"login\":\"esse_et\"}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("getParents")
    @CustomAuth
    void getParents_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getParents/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getParents_Summary, "getParents_whenEmpty_Anonim"));
    }

    @Test @Tag("getParents")
    @CustomUser(roles = Roles.HTEACHER)
    void getParents_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class, Answers.RETURNS_DEEP_STUBS);
        final Group group = mock(Group.class);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);
        when(sch1.getGroups().contains(any())).thenReturn(true);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(usersTest);

        mockMvc.perform(get("/parents/getParents/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyC\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"bodyP\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"name\":\"Силин А.К.\",\"par\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}},\"login\":\"facere_a\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}"))
            .andDo(defaultSwaggerDocs(getParents_Summary, "getParents_whenGood_HTEACHER"));
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * подтверждает клиенту права */
    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        mockMvc.perform(get("/parents/getInfo")
            .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
        .andExpect(status().isOk())
        .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    @Test @Tag("getInfoForHTeacher")
    @CustomAuth
    void getInfoForHTeacher_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getInfoFH")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * отправляет JSON'ом список групп учебного центра и подтверждает клиенту права */
    @Test @Tag("getInfoForHTeacher")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfoForHTeacher_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);
        when(sch1.getGroups()).thenReturn(TEST_UTILS.groups);

        mockMvc.perform(get("/parents/getInfoFH")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"firstG\":2323,\"bodyG\":{\"3456\":\"1Б\",\"4354\":\"1В\",\"2323\":\"1А\"}}"))
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenGood_HTEACHER"));
    }
}