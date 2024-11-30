package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import com.google.gson.JsonObject;
import config.CustomUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.data.models.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;
import static utils.RandomUtils.getCloneContacts;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({ContactsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ContactsControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private ContactsController contactsController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final RandomUtils randomUtils = new RandomUtils();
    private static final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @BeforeAll
    static void beforeAll() throws ServletException {
        authInjector.afterPropertiesSet();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.standaloneSetup(contactsController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    private RestDocumentationResultHandler chContact_Docs(String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("Изменение контакта + Server Sent Events")
            .description(defaultDescription)
            .tag("ContactsController")
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ContactsController/" + methodName, resource(snip.build()));
    }

    /** RU: админ для контактов сайта
     * клиент не делится нужными данными и получает 404 ответ */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenEmpty_Portal_AdminUser() throws Exception {
        mockMvc.perform(put("/contacts/chContact/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(chContact_Docs("chContact_whenEmpty_Portal_AdminUser"));
        verify(authController, times(0)).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для контактов школы
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser(roles = Roles.HTEACHER)
    void chContact_whenGood_YO_HTeacher() throws Exception {
        getSub().setLvlMore2("Yo");
        final User user = getSub().getUser();
        final School school = mock(School.class);
        user.getSelecRole().setYO(school);
        when(dbService.getSyst().getContacts())
            .thenReturn(getCloneContacts(randomUtils.contactsTest.get(0)));
        when(school.getContacts())
            .thenReturn(getCloneContacts(randomUtils.contactsTest.get(0)));
        mockMvc.perform(put("/contacts/chContact/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "p": "mapPr",
            "p1": "text",
            "val": "А проект вышел большим..."
        }
            """)).andExpect(status().isOk())
            .andDo(chContact_Docs("chContact_whenGood_YO_HTeacher"));
        verify(authController).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            answer.getValue().toString());
    }

    /** RU: админ для контактов сайта
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenGood_Portal_AdminUser() throws Exception {
        getSub().setLvlMore2("Por");
        when(dbService.getSyst().getContacts())
            .thenReturn(getCloneContacts(randomUtils.contactsTest.get(0)));
        mockMvc.perform(put("/contacts/chContact/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "p": "mapPr",
            "p1": "text",
            "val": "А проект вышел большим..."
        }
            """)).andExpect(status().isOk())
            .andDo(chContact_Docs("chContact_whenGood_Portal_AdminUser"));
        verify(authController).sendEventFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            answer.getValue().toString());
    }

    private RestDocumentationResultHandler getContacts_Docs(String methodName, boolean emptyResponse) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправка контактов, портала/школы")
            .description(defaultDescription)
            .tag("ContactsController")
            .pathParameters(parameterWithName("type")
                .type(SimpleType.STRING)
                .description("Нужный тип: Por - портал, Yo - школы")
            ).requestHeaders(headerWithName(SecurityConfig.authHeader)
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
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isNotFound())
            .andDo(getContacts_Docs("getContacts_whenEmpty_Portal_AdminUser", true));
    }

    /** RU: завуч для контактов школы
     * отправляет JSON'ом контакты клиенту */
    @Test @Tag("getContacts")
    @CustomUser(roles = Roles.HTEACHER)
    void getContacts_whenGood_YO_HTeacher() throws Exception {
        User user = getSub().getUser();
        School school = mock(School.class);
        user.getSelecRole().setYO(school);
        when(school.getContacts())
            .thenReturn(randomUtils.contactsTest.get(0));
        mockMvc.perform(get("/contacts/getContacts/{type}", "Yo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
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
            .thenReturn(randomUtils.contactsTest.get(0));
        mockMvc.perform(get("/contacts/getContacts/{type}", "Por")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"contact\":\"8 (800) 555 35 37\\n5 (353) 555 00 88\",\"mapPr\":{\"text\":\"Ближайшие станции метро:\\nАлександровский сад, 610 м (Филёвская линия, выход 5)\\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)\",\"imgUrl\":\"/static/media/map.jpg\"}}"))
            .andDo(getContacts_Docs("getContacts_whenGood_Portal_AdminUser", false));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class ContactsControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public ContactsController contactsController(AuthController authController) {
        return spy(new ContactsController(authController));
    }
}