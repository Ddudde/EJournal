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
import ru.data.reps.auth.RoleRepository;
import ru.services.db.DBService;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class AdminsControllerTest extends AbstractTestIntegration {
    private final DBService dbService;
    private final RoleRepository roleRepository;
    private static final String remPep_Summary = "Удаляет у пользователя роль администратора + Server Sent Events";
    private static final String chPep_Summary = "Изменяет фамилию пользователя + Server Sent Events";
    private static final String addPep_Summary = "Cоздаёт пользователя-администратора + Server Sent Events";
    private static final String getAdmins_Summary = "[start] Отправляет список администраторов";


    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Autowired
    public AdminsControllerTest(AdminsController adminsController, RoleRepository roleRepository, DBService dbService) {
        this.testController = adminsController;
        this.roleRepository = roleRepository;
        this.dbService = dbService;
        nameTestedClass = "AdminsController";
    }

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(delete("/admins/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * удаляет у пользователя роль администратора и отправляет JSON'ом инфу */
    @Test @Tag("remPep")
    @CustomUser
    void remPep_whenGood_Admin() throws Exception {
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userById(20L)).thenReturn(user);
        mockMvc.perform(delete("/admins/remPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764}",
            answer.getValue().toString());
    }

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(patch("/admins/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * изменяет фамилию пользователя и отправляет JSON'ом инфу */
    @Test @Tag("chPep")
    @CustomUser
    void chPep_whenGood_Admin() throws Exception {
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userById(20L)).thenReturn(user);
        mockMvc.perform(patch("/admins/chPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Дрыздов А.А.",
                "id": 20
            }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764,\"name\":\"Дрыздов А.А.\"}",
            answer.getValue().toString());
    }

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(post("/admins/addPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * добавляет нового админа и отправляет JSON'ом инфу */
    @Test @Tag("addPep")
    @CustomUser
    void addPep_whenGood_Admin() throws Exception {
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        mockMvc.perform(post("/admins/addPep")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Дрыздов А.А."
            }
            """))
            .andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"name\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    /** RU: аноним
     * не находит систему и отправляет 404 код-ответ */
    @Test @Tag("getAdmins")
    @CustomAuth
    void getAdmins_whenEmpty_Anonim() throws Exception {
        when(dbService.getSyst()).thenReturn(null);
        mockMvc.perform(get("/admins/getAdmins")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(getAdmins_Summary, "getAdmins_whenEmpty_Anonim"));
    }

    /** RU: админ
     * отправляет JSON'ом информацию о администраторах */
    @Test @Tag("getAdmins")
    @CustomUser
    void getAdmins_whenGood_Admin() throws Exception {
        when(dbService.getSyst().getAdmins()).thenReturn(usersTest);
        mockMvc.perform(get("/admins/getAdmins")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getAdmins_Summary, "getAdmins_whenGood_Admin"));
    }

}