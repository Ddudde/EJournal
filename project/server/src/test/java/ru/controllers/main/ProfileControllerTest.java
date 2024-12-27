package ru.controllers.main;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import com.google.gson.JsonObject;
import config.CustomAuth;
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
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.PushService;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({ProfileControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfileControllerTest {

    @Autowired
    private DBService dbService;

    @Autowired
    private PushService pushService;

    @Autowired
    private AuthController authController;

    @Autowired
    private ProfileController profileController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
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
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("ProfileController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ProfileController/" + methodName, resource(snip.build()));
    }

    private final String chKid_Summary = "Изменение контроллируемого ученика у родителя";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("chKid")
    @CustomAuth
    void chKid_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chKid")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(chKid_Summary, "chKid_whenEmpty_Anonim"));
    }

    /** RU: родитель
     * отправляет 200 код-ответ и меняет наблюдаемого ребёнка */
    @Test @Tag("chKid")
    @CustomUser(roles = Roles.PARENT)
    void chKid_whenGood_Parent() throws Exception {
        mockMvc.perform(patch("/profiles/chKid")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "idL": "123"
        }
                """))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"kid\":123}"))
            .andDo(default_Docs(chKid_Summary, "chKid_whenGood_Parent"));
    }

    private final String chRole_Summary = "Изменение роли на следующую по иерархии из имеющихся у пользователя";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("chRole")
    @CustomAuth
    void chRole_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chRole")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(chRole_Summary, "chRole_whenEmpty_Anonim"));
    }

    /** RU: ученик-администратор, с активной ролью ученика
     * отправляет 200 код-ответ и меняет роль на администратора */
    @Test @Tag("chRole")
    @CustomUser
    void chRole_whenGood_Kid() throws Exception {
        getSub().getUser().setSelRole(Roles.KID);

        mockMvc.perform(patch("/profiles/chRole")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"role\":4}"))
            .andDo(default_Docs(chRole_Summary, "chRole_whenGood_Kid"));
    }

    private final String exit_Summary = "Выход с аккаунта";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("exit")
    @CustomAuth
    void exit_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/exit")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(exit_Summary, "exit_whenEmpty_Anonim"));
    }

    /** RU: админ
     * отправляет 200 код-ответ */
    @Test @Tag("exit")
    @CustomUser
    void exit_whenGood_Admin() throws Exception {
        mockMvc.perform(patch("/profiles/exit")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "notifToken": "notifTest"
        }
            """))
            .andExpect(status().isOk())
            .andDo(default_Docs(exit_Summary, "exit_whenGood_Admin"));
        verify(pushService).remToken(any(), eq("notifTest"));
    }

    private final String chEmail_Summary = "Изменение/добавление электронной почты определённой роли пользователя + Server Sent Events";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("chEmail")
    @CustomAuth
    void chEmail_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chEmail")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(chEmail_Summary, "chEmail_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * отправляет JSON'ом инфу о электронной почте */
    @Test @Tag("chEmail")
    @CustomUser
    void chEmail_whenGood_Admin() throws Exception {
        mockMvc.perform(patch("/profiles/chEmail")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "email": "mail1@example.com"
        }
            """))
            .andExpect(status().isOk())
            .andDo(default_Docs(chEmail_Summary, "chEmail_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chEmail"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"email\":\"mail1@example.com\",\"role\":4}}",
            answer.getValue().toString());
    }

    private final String chInfo_Summary = "Изменение/добавление дополнительной информации о пользователе + Server Sent Events";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("chInfo")
    @CustomAuth
    void chInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(chInfo_Summary, "chInfo_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * отправляет JSON'ом инфу о дополнительной инфе */
    @Test @Tag("chInfo")
    @CustomUser
    void chInfo_whenGood_Admin() throws Exception {
        mockMvc.perform(patch("/profiles/chInfo")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "info": "testInfo"
        }
            """))
            .andExpect(status().isOk())
            .andDo(default_Docs(chInfo_Summary, "chInfo_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"more\":\"testInfo\"}}",
            answer.getValue().toString());
    }

    private final String chLogin_Summary = "Изменение логина пользователя + Server Sent Events";

    /** RU: аноним
     * отправляет 401 код-ответ */
    @Test @Tag("chLogin")
    @CustomAuth
    void chLogin_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(patch("/profiles/chLogin")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(default_Docs(chLogin_Summary, "chLogin_whenEmpty_Anonim"));
        verify(authController, times(0)).sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * отправляет JSON'ом информацию о старом и новом логине */
    @Test @Tag("chLogin")
    @CustomUser
    void chLogin_whenGood_Admin() throws Exception {
        getSub().setLvlMore2("nm12");
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(patch("/profiles/chLogin")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "nLogin": nm
        }
            """))
            .andExpect(status().isOk())
            .andDo(default_Docs(chLogin_Summary, "chLogin_whenGood_Admin"));
        verify(authController).sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"oLogin\":\"nm12\",\"nLogin\":\"nm\"}}",
            answer.getValue().toString());
    }

    private RestDocumentationResultHandler getProfile_Docs(String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправляет инфу профиля либо другого пользователя либо личную")
            .description(defaultDescription)
            .pathParameters(parameterWithName("login").optional()
                .type(SimpleType.STRING)
                .description("Логин пользователя")
            )
            .tag("ProfileController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ProfileController/" + methodName, resource(snip.build()));
    }

    /** RU: аноним пытается посмотреть другого
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomAuth
    void getProfile_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(getProfile_Docs("getProfile_whenEmpty_Anonim"));
    }

    /** RU: админ смотрит себя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomUser
    void getProfile_whenGood_AuthLogin_Admin() throws Exception {
        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"login\":\"nm12\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_AuthLogin_Admin"));
    }

    /** RU: аноним смотрит пользователя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomAuth
    void getProfile_whenGood_CustomLogin_Anonim() throws Exception {
        when(dbService.userByLogin("nm12")).thenReturn(randomUtils.usersTest.get(4));

        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"login\":\"facere_a\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_CustomLogin_Anonim"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class ProfileControllerConfig {

    @Bean
    public PushService pushService() {
        return mock(PushService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService, PushService pushService) {
        return spy(new MainService(pushService, dbService, null));
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public ProfileController profileController(AuthController authController) {
        return spy(new ProfileController(authController));
    }
}