package ru.controllers.main;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.services.EmailService;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;

import javax.servlet.ServletException;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;
import static utils.TestUtils.getSub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({SettingsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SettingsControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private DBService dbService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PushService pushService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SettingsController settingsController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
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
            .tag("SettingsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("SettingsController/" + methodName, resource(snip.build()));
    }

    private final String checkCodeEmail_Summary = "Подтверждение емэйла";

    @Test @Tag("checkCodeEmail")
    @CustomAuth
    void checkCodeEmail_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        
        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkCodeEmail_Summary, "checkCodeEmail_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * сверяет код подтверждения и меняет емейл */
    @Test @Tag("checkCodeEmail")
    @CustomUser
    void checkCodeEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        User user = getSub().getUser();
        when(dbService.userByCode("uuid")).thenReturn(user);
        user.getSettings().setEmailCode("code");

        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "invCod": "uuid",
                "emailCode": "code",
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkCodeEmail_Summary, "checkCodeEmail_whenGood_AdminUser"));
    }

    private final String startEmail_Summary = "Изменение электронной почты пользователя или добавление при регистрации";

    @Test @Tag("startEmail")
    @CustomAuth
    void startEmail_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        
        mockMvc.perform(patch("/settings/startEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(startEmail_Summary, "startEmail_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * отправляет код на почту при регистрации */
    @Test @Tag("startEmail")
    @CustomUser
    void startEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        User user = getSub().getUser();
        when(dbService.userByCode("uuid")).thenReturn(user);

        mockMvc.perform(patch("/settings/startEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "invCod": "uuid",
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(startEmail_Summary, "startEmail_whenGood_AdminUser"));

        verify(emailService, times(1)).sendRegCode(eq("test@mail.com"), any());
    }

    private final String remNotifToken_Summary = "Удаление токена уведомлений";

    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(post("/settings/remNotifToken/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remNotifToken_Summary, "remNotifToken_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * удаляет токен клиента */
    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        
        mockMvc.perform(post("/settings/remNotifToken/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "testtoken"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remNotifToken_Summary, "remNotifToken_whenGood_AdminUser"));

        verify(pushService, times(1)).remToken(any(), eq("testtoken"));
    }

    private final String addNotifToken_Summary = "Установка токена уведомлений";

    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(post("/settings/addNotifToken/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))

            .andExpect(statusCode)

            .andDo(defaultSwaggerDocs(addNotifToken_Summary, "addNotifToken_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * устанавливает токен для уведомлений клиента */
    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        
        mockMvc.perform(post("/settings/addNotifToken/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "testtoken"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addNotifToken_Summary, "addNotifToken_whenGood_AdminUser"));

        verify(pushService, times(1)).addToken(any(), eq("testtoken"));
    }

    private final String chSettings_Summary = "Вкл/выкл подсказки или ряд уведомлений";

    /** RU: админ
     * клиент не отправляет данных и получает 404-ый код */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(patch("/settings/chSettings/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSettings_Summary, "chSettings_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * включает уведомления о новых заявках на подключение школ */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = mock(SettingUser.class);
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chSettings/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "checkbox_notify_new_sch",
                "val": true
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSettings_Summary, "chSettings_whenGood_AdminUser"));
        verify(settingUser, times(1)).setNNewReqSch(eq(true));
    }

    private final String checkPasCodeEmail_Summary = "Изменяет пароль пользователя при помощи емэйла";

    /** RU: админ
     * не подтверждает код с эмейла и отправляет 404 ответ */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenWrongCode_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("11112");
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "emailCode": "1111",
                "nPar": "1234"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkPasCodeEmail_Summary, "checkPasCodeEmail_whenWrongCode_AdminUser"));

        verify(user, times(0)).setPassword(eq("1234"));
    }

    /** RU: админ
     * подтверждает код с эмейла и меняет пароль пользователя */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("1111");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailCode": "1111",
                "nPar": "1234"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkPasCodeEmail_Summary, "checkPasCodeEmail_whenGood_AdminUser"));

        verify(passwordEncoder, times(1)).encode(eq("1234"));
    }

    private final String chPass_Summary = "Изменяет пароль пользователя при помощи емэйла/секретной фразы";

    /** RU: админ
     * неуспешно сверяет секретную фразу и отправляет клиенту причину */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenError_secFr_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isAccepted();
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret1");
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "emailSt": false,
                "secFR": "victoria_secret"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{\"error\":\"secFr\"}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenError_secFr_AdminUser"));
    }

    /** RU: админ
     * сверяет секретную фразу и меняет пароль  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_secFr_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailSt": false,
                "secFR": "victoria_secret",
                "nPar": "passs"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenGood_secFr_AdminUser"));

        verify(passwordEncoder, times(1)).encode(eq("passs"));
    }

    /** RU: админ
     * отправляет код подтверждения на почту  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_Email_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmail("test@mail.com");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailSt": true,
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenGood_Email_AdminUser"));
        verify(emailService, times(1)).sendRecCode(eq("test@mail.com"), any(), any());
    }

    private final String getSettings_Summary = "Отправляет настройки клиенту";

    /** RU: админ
     * клиент не отправляет данные и получает 404 ответ */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        when(getSub().getUser().getSettings()).thenReturn(null);

        mockMvc.perform(get("/settings/getSettings/")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getSettings_Summary, "getSettings_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * отправляет JSON'ом настройки пользователя клиенту */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setNNewReqSch(true);
        when(getSub().getUser().getSettings()).thenReturn(settingUser);

        mockMvc.perform(get("/settings/getSettings/")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"checkbox_hints\":true,\"checkbox_notify\":false,\"checkbox_notify_sched\":false,\"checkbox_notify_marks\":false,\"checkbox_notify_yo\":false,\"checkbox_notify_por\":false,\"checkbox_notify_new_sch\":true}"))
            .andDo(defaultSwaggerDocs(getSettings_Summary, "getSettings_whenGood_AdminUser"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SettingsControllerConfig {

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    public PushService pushService() {
        return mock(PushService.class);
    }

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService, EmailService emailService, PushService pushService) {
        return spy(new MainService(pushService, dbService, emailService));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return spy(new BCryptPasswordEncoder(8));
    }

    @Bean
    public IniDBService iniDBService() {
        return mock(IniDBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public SettingsController settingsController(PasswordEncoder passwordEncoder) {
        return spy(new SettingsController(passwordEncoder));
    }
}