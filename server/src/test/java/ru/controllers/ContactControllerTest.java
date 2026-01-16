package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.security.user.Roles;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class ContactControllerTest extends AbstractTestIntegration {
    private static final String CH_CONTACT_SUMMARY = "Изменение контакта + Server Sent Events";
    private final IDBService dbService;
    private final ISSEService sseService;

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    ContactControllerTest(ContactController contactController, IDBService dbService, ISSEService sseService) {
        this.sseService = sseService;
        this.testController = contactController;
        this.dbService = dbService;
        nameTestedClass = "ContactsController";
    }

    /** RU: админ для контактов сайта
     * клиент не делится нужными данными и получает 404 ответ */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenEmpty_Portal_AdminUser() throws Exception {
        mockMvc.perform(put("/contacts/chContact/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(CH_CONTACT_SUMMARY, "chContact_whenEmpty_Portal_AdminUser"));
        verify(sseService, times(0)).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для контактов школы
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser(roles = Roles.HTEACHER)
    void chContact_whenGood_YO_HTeacher() throws Exception {
        getSub().setLvlMore2("Yo");
        final User user = dbService.userById(getSub().getUserId());
        final School school = mock(School.class);
        user.getSelecRole().setYO(school);
        when(dbService.getSyst().getContacts())
            .thenReturn(getCloneContacts(TEST_UTILS.contactsTest.get(0)));
        when(school.getContacts())
            .thenReturn(getCloneContacts(TEST_UTILS.contactsTest.get(0)));

        mockMvc.perform(put("/contacts/chContact/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "p": "mapPr",
                "p1": "text",
                "val": "А проект вышел большим..."
            }
            """))
                .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(CH_CONTACT_SUMMARY, "chContact_whenGood_YO_HTeacher"));

        verify(sseService).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            gson.toJson(answer.getValue()));
    }

    /** RU: админ для контактов сайта
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenGood_Portal_AdminUser() throws Exception {
        getSub().setLvlMore2("Por");
        when(dbService.getSyst().getContacts())
            .thenReturn(getCloneContacts(TEST_UTILS.contactsTest.getFirst()));

        mockMvc.perform(put("/contacts/chContact")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "p": "mapPr",
                "p1": "text",
                "val": "А проект вышел большим..."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(CH_CONTACT_SUMMARY, "chContact_whenGood_Portal_AdminUser"));

        verify(sseService).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            gson.toJson(answer.getValue()));
    }

    private RestDocumentationResultHandler getContacts_Docs(String methodName, boolean emptyResponse) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправка контактов, портала/школы")
            .description(defaultDescription)
            .tag("ContactsController")
            .pathParameters(parameterWithName("type")
                .type(SimpleType.STRING)
                .description("Нужный тип: Por - портал, Yo - школы")
            ).requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        if(!emptyResponse) {
            snip.responseFields(fieldWithPath("contact").description(""),
                fieldWithPath("mapPr").description(""),
                fieldWithPath("mapPr.text").description(""),
                fieldWithPath("mapPr.imgUrl").description("")
            );
        }
        return document("ContactsController/" + methodName, resource(snip.build()));
    }

    /** RU: админ для контактов сайта
     * клиент не делится нужными данными и получает 404 ответ */
    @Test @Tag("getContacts")
    @CustomUser
    void getContacts_whenEmpty_Portal_AdminUser() throws Exception {
        when(dbService.getSyst()).thenReturn(null);

        mockMvc.perform(get("/contacts/getContacts/{type}", "Por")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isNotFound())
            .andDo(getContacts_Docs("getContacts_whenEmpty_Portal_AdminUser", true));
    }

    /** RU: завуч для контактов школы
     * отправляет JSON'ом контакты клиенту */
    @Test @Tag("getContacts")
    @CustomUser(roles = Roles.HTEACHER)
    void getContacts_whenGood_YO_HTeacher() throws Exception {
        User user = dbService.userById(getSub().getUserId());
        School school = mock(School.class);
        user.getSelecRole().setYO(school);
        when(school.getContacts())
            .thenReturn(TEST_UTILS.contactsTest.get(0));

        mockMvc.perform(get("/contacts/getContacts/{type}", "Yo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"contact\":\"8 (800) 555 35 37\\n5 (353) 555 00 88\",\"mapPr\":{\"text\":\"Ближайшие станции метро:\\nАлександровский сад, 610 м (Филёвская линия, выход 5)\\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)\",\"imgUrl\":\"/static/media/map.jpg\"}}"))
            .andDo(getContacts_Docs("getContacts_whenGood_YO_HTeacher", false));
    }

    /** RU: админ для контактов сайта
     * отправляет JSON'ом контакты клиенту */
    @Test @Tag("getContacts")
    @CustomUser
    void getContacts_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.getSyst().getContacts())
            .thenReturn(TEST_UTILS.contactsTest.get(0));

        mockMvc.perform(get("/contacts/getContacts/{type}", "Por")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"contact\":\"8 (800) 555 35 37\\n5 (353) 555 00 88\",\"mapPr\":{\"text\":\"Ближайшие станции метро:\\nАлександровский сад, 610 м (Филёвская линия, выход 5)\\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)\",\"imgUrl\":\"/static/media/map.jpg\"}}"))
            .andDo(getContacts_Docs("getContacts_whenGood_Portal_AdminUser", false));
    }
}