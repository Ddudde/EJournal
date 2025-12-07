package ru.controllers.school;

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
import static utils.TestUtils.getCloneRequests;

public class RequestControllerTest extends AbstractTestIntegration {
    private final DBService dbService;
    private static final String addReq_Summary = "Добавляет заявку + Server Sent Events";
    private static final String delReq_Summary = "Удаление заявки + Server Sent Events";
    private static final String chTitle_Summary = "Изменение заголовка заявки + Server Sent Events";
    private static final String chDate_Summary = "Изменение даты заявки + Server Sent Events";
    private static final String chText_Summary = "Изменение текста заявки + Server Sent Events";
    private static final String getRequests_Summary = "[start] Отправляет инфу о заявках";

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Autowired
    public RequestControllerTest(DBService dbService, RequestController requestController) {
        this.dbService = dbService;
        this.testController = requestController;
        nameTestedClass = "RequestController";
    }

   @Test @Tag("addReq")
    @CustomAuth
    void addReq_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(post("/requests/addReq")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(addReq_Summary, "addReq_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addReq"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * добавляет заявку и отправляет JSON'ом удалённую заявку */
    @Test @Tag("addReq")
    @CustomUser
    void addReq_whenGood_Admin() throws Exception {
        mockMvc.perform(post("/requests/addReq")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "email": "mail@mail.com",
                "date": "11.11.1111",
                "fio": "Дрыздов А.А."
            }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(addReq_Summary, "addReq_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("addReq"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"title\":\"mail@mail.com\",\"date\":\"11.11.1111\",\"text\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    @Test @Tag("delReq")
    @CustomAuth
    void delReq_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/requests/delReq")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(delReq_Summary, "delReq_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("delReq"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * удаляет заявку и отправляет JSON'ом удалённую заявку */
    @Test @Tag("delReq")
    @CustomUser
    void delReq_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(getCloneRequests(TEST_UTILS.requestTest.get(0)));

        mockMvc.perform(delete("/requests/delReq")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delReq_Summary, "delReq_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("delReq"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":352}",
            answer.getValue().toString());
    }

    @Test @Tag("chTitle")
    @CustomAuth
    void chTitle_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chTitle")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chTitle_Summary, "chTitle_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chTitle"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * изменяет заголовок заявки и отправляет JSON'ом изменения */
    @Test @Tag("chTitle")
    @CustomUser
    void chTitle_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(getCloneRequests(TEST_UTILS.requestTest.get(0)));

        mockMvc.perform(patch("/requests/chTitle")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "title": "example@pepl.qq",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chTitle_Summary, "chTitle_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chTitle"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":352,\"title\":\"example@pepl.qq\"}",
            answer.getValue().toString());
    }

    @Test @Tag("chDate")
    @CustomAuth
    void chDate_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chDate")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chDate_Summary, "chDate_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chDate"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * изменяет дату заявки и отправляет JSON'ом изменения */
    @Test @Tag("chDate")
    @CustomUser
    void chDate_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(getCloneRequests(TEST_UTILS.requestTest.get(0)));

        mockMvc.perform(patch("/requests/chDate")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "date": "01.01.2001",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chDate_Summary, "chDate_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chDate"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":352,\"date\":\"01.01.2001\"}",
            answer.getValue().toString());
    }

    @Test @Tag("chText")
    @CustomAuth
    void chText_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chText")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chText_Summary, "chText_whenEmpty_Anonim"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chText"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * изменяет текст заявки и отправляет JSON'ом изменения */
    @Test @Tag("chText")
    @CustomUser
    void chText_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(getCloneRequests(TEST_UTILS.requestTest.get(0)));

        mockMvc.perform(patch("/requests/chText")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "text": "Дроздич Г.Г.",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chText_Summary, "chText_whenGood_Admin"));
        staticMockSSE.verify(() -> SSEController.sendEventFor(eq("chText"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":352,\"text\":\"Дроздич Г.Г.\"}",
            answer.getValue().toString());
    }

    @Test @Tag("getRequests")
    @CustomAuth
    void getRequests_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/requests/getRequests")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getRequests_Summary, "getRequests_whenEmpty_Anonim"));
    }

    /** RU: админ
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getRequests")
    @CustomUser
    void getRequests_whenGood_Admin() throws Exception {
        when(dbService.getRequests()).thenReturn(TEST_UTILS.requestTest);

        mockMvc.perform(get("/requests/getRequests")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"352\":{\"title\":\"mail1@mail.com\",\"date\":\"11.11.2011\",\"text\":\"Дроздов А.А.\"},\"3872\":{\"title\":\"mail10@mail.com\",\"date\":\"11.01.2011\",\"text\":\"Силин А.К.\"},\"9764\":{\"title\":\"mail11@mail.com\",\"date\":\"01.11.2011\",\"text\":\"Пестов Л.А.\"}}"))
            .andDo(defaultSwaggerDocs(getRequests_Summary, "getRequests_whenGood_Admin"));
    }
}