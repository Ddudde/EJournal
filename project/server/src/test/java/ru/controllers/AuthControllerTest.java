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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.CustomToken;
import ru.services.EmailService;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;
import java.util.UUID;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({AuthControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private MainService mainService;

    @Autowired
    private AuthController authController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @Captor
    private ArgumentCaptor<Object> obj;

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
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    private RestDocumentationResultHandler default_Docs(String summary, String methodName) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("AuthController").requestFields()
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("AuthController/" + methodName, resource(snip.build()));
    }

    private RestDocumentationResultHandler start_Docs(String methodName) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start#1] Открытие Server Sent Events для нового клиента или сохранение подписки для старого пользователя")
            .description(defaultDescription + """
            Подписка сохраняется в течении одного запуска сервера.
            """)
            .pathParameters(parameterWithName("uuidAuth").optional()
                .type(SimpleType.STRING)
                .description("Авторизация, в ней подписка и пользователь")
            )
            .tag("AuthController");
        return document("AuthController/" + methodName, resource(snip.build()));
    }

    /** RU: стартует для нового клиента */
    @Test @Tag("start")
    void start_whenGoodNext_Anonim() throws Exception {
        mockMvc.perform(get("/auth/start"))
            .andExpect(status().isOk())
            .andDo(start_Docs("start_whenGoodNext_Anonim"));
        assertNotEquals(0, mainService.subscriptions.size());
    }

    /** RU: стартует со старой подпиской */
    @Test @Tag("start")
    @CustomUser
    void start_whenGood_AdminUser() throws Exception {
        final CustomToken cu = ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication());
        final String uuid = cu.getUUID();
        final Subscriber sub = mock(Subscriber.class, Answers.RETURNS_DEEP_STUBS);
        when(sub.getLogin()).thenReturn("nm12");
        mainService.subscriptions.put(UUID.fromString(uuid), sub);

        mockMvc.perform(get("/auth/start/{uuidAuth}", uuid))
            .andExpect(status().isOk())
            .andDo(start_Docs("start_whenGood_AdminUser"));
        verify(sub).setSSE(any(), eq(UUID.fromString(uuid)));
    }

    private final String infCon_Summary = "[start#2] Изменение подписки";

    /** RU: старая авторизованная подписка пользователя существует
     * новая заменяется старой */
    @Test @Tag("infCon")
    @CustomUser
    void infCon_whenGood_AdminUser() throws Exception {
        mockMvc.perform(patch("/auth/infCon/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "type": "TUTOR",
            "notifToken": "passTest",
            "permis": true
        }
                """))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"role\":4}"))
            .andDo(default_Docs(infCon_Summary, "infCon_whenGood_AdminUser"));
    }

    private final String remCon_Summary = "Завершение сеанса";

    /** RU: админ
     * завершение сеанса */
    @Test @Tag("remCon")
    @CustomUser
    void remCon_whenGood_AdminUser() throws Exception {
        final SseEmitter sseEmitter = mock(SseEmitter.class);
        getSub().setSSE(sseEmitter);

        mockMvc.perform(patch("/auth/remCon/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andDo(default_Docs(remCon_Summary, "remCon_whenGood_AdminUser"));
    }

    private final String auth_Summary = "Авторизация пользователя";

    /** RU: не успешная авторизация пользователя */
    @Test @Tag("auth")
    @CustomUser
    void auth_whenWrong_AdminUser() throws Exception {
        final User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getPassword()).thenReturn("passTest1");

        mockMvc.perform(post("/auth/auth/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "notifToken": "uuidTest",
            "password": "passTest",
            "permis": true
        }
            """)).andExpect(status().isNotFound())
            .andDo(default_Docs(auth_Summary, "auth_whenWrong_AdminUser"));
    }

    /** RU: админ
     * авторизация пользователя */
    @Test @Tag("auth")
    @CustomUser(password = "passTest")
    void auth_whenGood_AdminUser() throws Exception {
        final String uuid = ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getUUID();

        mockMvc.perform(post("/auth/auth/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "notifToken": "uuidTest",
            "password": "passTest",
            "permis": true
        }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"auth\":true,\"login\":\"nm12\",\"role\":4,\"uuidS\":\"" + uuid + "\",\"roles\":true,\"secFr\":false,\"email\":false}"))
            .andDo(default_Docs(auth_Summary, "auth_whenGood_AdminUser"));
    }

    private final String reg_Summary = "Регистрация пользователя";

    /** RU: существующий логин */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenWrongLogin_Anonim() throws Exception {
        final User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isNotFound())
            .andDo(default_Docs(reg_Summary, "reg_whenWrongLogin_Anonim"));
    }

    /** RU: инвайт неверный */
    @Test @Tag("reg")
    void reg_whenWrong_Anonim() throws Exception {
        when(dbService.userByCode("uuidTest")).thenReturn(null);
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isAccepted())
            .andExpect(content().string("{\"error\":\"noInv\"}"))
            .andDo(default_Docs(reg_Summary, "reg_whenWrong_Anonim"));
    }

    /** RU: регистирирует пользователя нового */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenGood_Anonim() throws Exception {
        final User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isCreated())
            .andDo(default_Docs(reg_Summary, "reg_whenGood_Anonim"));
    }

    private final String checkInvCode_Summary = "Проверка инвайта для регистрации/регистрации новой роли";

    /** RU: админ
     * не успешно проверяет инвайт код для пользователя и отправляет 404 код клиенту */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenWrong_AdminUser() throws Exception {
        when(dbService.userByCode(null)).thenReturn(null);

        mockMvc.perform(post("/auth/checkInvCode/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(checkInvCode_Summary, "checkInvCode_whenWrong_AdminUser"));
    }

    /** RU: админ
     * успешно проверяет инвайт код для пользователя */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenGood_AdminUser() throws Exception {
        final User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);

        mockMvc.perform(post("/auth/checkInvCode/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "code": "uuidTest"
        }
            """)).andExpect(status().isOk())
            .andDo(default_Docs(checkInvCode_Summary, "checkInvCode_whenGood_AdminUser"));
    }

    private final String setCodePep_Summary = "Установка/обновление инвайта для регистрации + Server Sent Events";

    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenEmpty_AdminUser() throws Exception {
        when(dbService.userByLogin(null)).thenReturn(null);

        mockMvc.perform(patch("/auth/setCodePep/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(setCodePep_Summary, "setCodePep_whenEmpty_AdminUser"));
        verify(authController, times(0)).sendEventFor(any(), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenGood_AdminUser() throws Exception {
        final User user = getSub().getUser();

        mockMvc.perform(patch("/auth/setCodePep/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": "nm12"
        }
            """)).andExpect(status().isOk())
            .andDo(default_Docs(setCodePep_Summary, "setCodePep_whenGood_AdminUser"));
        verify(user).setCode((String) obj.capture());
        verify(authController, times(2)).sendEventFor(any(), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"code\":\"%s\",\"id1\":0}".formatted(obj.getValue().toString()),
            answer.getValue().toString());
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class AuthControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    public PushService pushService() {
        return mock(PushService.class);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService, EmailService emailService, PushService pushService) {
        return new MainService(pushService, dbService, emailService);
    }

    @Bean
    public IniDBService iniDBService(MainService mainService) {
        return mock(IniDBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public AuthController authController() {
        return spy(new AuthController());
    }
}