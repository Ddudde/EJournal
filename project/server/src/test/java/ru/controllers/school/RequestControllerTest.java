package ru.controllers.school;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
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
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.AuthController;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.services.MainService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({RequestControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final RandomUtils randomUtils = new RandomUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private RequestController requestController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;
    
    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(requestController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .setCustomArgumentResolvers(subscriberMethodArgumentResolver)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    /** RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     * @param summary Заголовок эндпоинта
     * @param methodName Название теста
     * @return Сниппет */
    private RestDocumentationResultHandler defaultSwaggerDocs(String summary, String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("RequestController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("RequestController/" + methodName, resource(snip.build()));
    }

    private final String addReq_Summary = "Добавляет заявку + Server Sent Events";

   @Test @Tag("addReq")
    @CustomAuth
    void addReq_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(post("/requests/addReq")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(addReq_Summary, "addReq_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("addReq"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * добавляет заявку и отправляет JSON'ом удалённую заявку */
    @Test @Tag("addReq")
    @CustomUser
    void addReq_whenGood_Admin() throws Exception {
        mockMvc.perform(post("/requests/addReq")
                .header(SecurityConfig.authTokenHeader, bearerToken)
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
        verify(authController).sendEventFor(eq("addReq"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":null,\"body\":{\"title\":\"mail@mail.com\",\"date\":\"11.11.1111\",\"text\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    private final String delReq_Summary = "Удаление заявки + Server Sent Events";

    @Test @Tag("delReq")
    @CustomAuth
    void delReq_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/requests/delReq")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(delReq_Summary, "delReq_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("delReq"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * удаляет заявку и отправляет JSON'ом удалённую заявку */
    @Test @Tag("delReq")
    @CustomUser
    void delReq_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(randomUtils.requestTest.get(0));

        mockMvc.perform(delete("/requests/delReq")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delReq_Summary, "delReq_whenGood_Admin"));
        verify(authController).sendEventFor(eq("delReq"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":352}",
            answer.getValue().toString());
    }

    private final String chTitle_Summary = "Изменение заголовка заявки + Server Sent Events";

    @Test @Tag("chTitle")
    @CustomAuth
    void chTitle_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chTitle")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chTitle_Summary, "chTitle_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chTitle"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * изменяет заголовок заявки и отправляет JSON'ом изменения */
    @Test @Tag("chTitle")
    @CustomUser
    void chTitle_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(randomUtils.requestTest.get(0));

        mockMvc.perform(patch("/requests/chTitle")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "title": "example@pepl.qq",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chTitle_Summary, "chTitle_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chTitle"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":352,\"title\":\"example@pepl.qq\"}",
            answer.getValue().toString());
    }

    private final String chDate_Summary = "Изменение даты заявки + Server Sent Events";

    @Test @Tag("chDate")
    @CustomAuth
    void chDate_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chDate")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chDate_Summary, "chDate_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chDate"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * изменяет дату заявки и отправляет JSON'ом изменения */
    @Test @Tag("chDate")
    @CustomUser
    void chDate_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(randomUtils.requestTest.get(0));

        mockMvc.perform(patch("/requests/chDate")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "date": "01.01.2001",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chDate_Summary, "chDate_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chDate"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":352,\"date\":\"01.01.2001\"}",
            answer.getValue().toString());
    }


    private final String chText_Summary = "Изменение текста заявки + Server Sent Events";

    @Test @Tag("chText")
    @CustomAuth
    void chText_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/requests/chText")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chText_Summary, "chText_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chText"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * изменяет текст заявки и отправляет JSON'ом изменения */
    @Test @Tag("chText")
    @CustomUser
    void chText_whenGood_Admin() throws Exception {
        when(dbService.requestById(20L)).thenReturn(randomUtils.requestTest.get(0));

        mockMvc.perform(patch("/requests/chText")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "text": "Дроздич Г.Г.",
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chText_Summary, "chText_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chText"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":352,\"text\":\"Дроздич Г.Г.\"}",
            answer.getValue().toString());
    }

    private final String getRequests_Summary = "[start] Отправляет инфу о заявках";

    @Test @Tag("getRequests")
    @CustomAuth
    void getRequests_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/requests/getRequests")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getRequests_Summary, "getRequests_whenEmpty_Anonim"));
    }

    /** RU: админ
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getRequests")
    @CustomUser
    void getRequests_whenGood_Admin() throws Exception {
        when(dbService.getRequests()).thenReturn(randomUtils.requestTest);

        mockMvc.perform(get("/requests/getRequests")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"352\":{\"title\":\"mail1@mail.com\",\"date\":\"11.11.2011\",\"text\":\"Дроздов А.А.\"},\"3872\":{\"title\":\"mail10@mail.com\",\"date\":\"11.01.2011\",\"text\":\"Силин А.К.\"},\"9764\":{\"title\":\"mail11@mail.com\",\"date\":\"01.11.2011\",\"text\":\"Пестов Л.А.\"}}"))
            .andDo(defaultSwaggerDocs(getRequests_Summary, "getRequests_whenGood_Admin"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class RequestControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public IniDBService iniDBService() {
        return mock(IniDBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public RequestController requestController(AuthController authController) {
        return spy(new RequestController(authController));
    }
}