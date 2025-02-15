package ru.controllers.main;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.SSEController;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import utils.TestUtils;

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
import static utils.TestUtils.defaultDescription;
import static utils.TestUtils.getSub;

@Slf4j
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({ProfileControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfileControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";
    private MockedStatic theMock;

    @Autowired
    private DBService dbService;

    @Autowired
    private PushService pushService;

    @Autowired
    private ProfileController profileController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @AfterEach
    void afterEach() {
        theMock.close();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        theMock = Mockito.mockStatic(SSEController.class);
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
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
            .tag("ProfileController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ProfileController/" + methodName, resource(snip.build()));
    }

    private final String chKid_Summary = "Изменение контроллируемого ученика у родителя";

    @Test @Tag("chKid")
    @CustomAuth
    void chKid_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chKid")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chKid_Summary, "chKid_whenEmpty_Anonim"));
    }

    @Test @Tag("chKid")
    @CustomUser(roles = Roles.PARENT)
    void chKid_whenGood_Parent() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chKid")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "idL": "123"
            }
            """)).andExpect(statusCode)
            .andExpect(content().json("{\"kid\":123}"))
            .andDo(defaultSwaggerDocs(chKid_Summary, "chKid_whenGood_Parent"));
    }

    private final String chRole_Summary = "Изменение роли на следующую по иерархии из имеющихся у пользователя";

    @Test @Tag("chRole")
    @CustomAuth
    void chRole_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chRole")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chRole_Summary, "chRole_whenEmpty_Anonim"));
    }

    /** RU: ученик-администратор, с активной ролью ученика
     * отправляет 200 код-ответ и меняет роль на администратора */
    @Test @Tag("chRole")
    @CustomUser
    void chRole_whenGood_Kid() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        dbService.userById(getSub().getUserId()).setSelRole(Roles.KID);

        mockMvc.perform(patch("/profiles/chRole")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"role\":4}"))
            .andDo(defaultSwaggerDocs(chRole_Summary, "chRole_whenGood_Kid"));
    }

    private final String exit_Summary = "Выход с аккаунта";

    @Test @Tag("exit")
    @CustomAuth
    void exit_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/exit")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(exit_Summary, "exit_whenEmpty_Anonim"));
    }

    @Test @Tag("exit")
    @CustomUser
    void exit_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/exit")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "notifTest"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(exit_Summary, "exit_whenGood_Admin"));
        verify(pushService).remToken(any(), eq("notifTest"));
    }

    private final String chEmail_Summary = "Изменение/добавление электронной почты определённой роли пользователя + Server Sent Events";

    @Test @Tag("chEmail")
    @CustomAuth
    void chEmail_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chEmail")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chEmail_Summary, "chEmail_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    @Test @Tag("chEmail")
    @CustomUser
    void chEmail_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chEmail")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "email": "mail1@example.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chEmail_Summary, "chEmail_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chEmail"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"body\":{\"email\":\"mail1@example.com\",\"role\":4}}",
            answer.getValue().toString());
    }

    private final String chInfo_Summary = "Изменение/добавление дополнительной информации о пользователе + Server Sent Events";

    @Test @Tag("chInfo")
    @CustomAuth
    void chInfo_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chInfo_Summary, "chInfo_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    @Test @Tag("chInfo")
    @CustomUser
    void chInfo_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "info": "testInfo"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chInfo_Summary, "chInfo_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"body\":{\"more\":\"testInfo\"}}",
            answer.getValue().toString());
    }

    private final String chLogin_Summary = "Изменение логина пользователя + Server Sent Events";

    @Test @Tag("chLogin")
    @CustomAuth
    void chLogin_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();

        mockMvc.perform(patch("/profiles/chLogin")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chLogin_Summary, "chLogin_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * отправляет JSON'ом информацию о старом и новом логине */
    @Test @Tag("chLogin")
    @CustomUser
    void chLogin_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        getSub().setLvlMore2("nm12");
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(patch("/profiles/chLogin")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "nLogin": nm
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chLogin_Summary, "chLogin_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any()));
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
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andDo(getProfile_Docs("getProfile_whenEmpty_Anonim"));
    }

    /** RU: админ смотрит себя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomUser
    void getProfile_whenGood_AuthLogin_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"login\":\"nm12\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_AuthLogin_Admin"));
    }

    /** RU: аноним смотрит пользователя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomAuth
    void getProfile_whenGood_CustomLogin_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        when(dbService.userByLogin("nm12")).thenReturn(TestUtils.usersTest.get(4));

        mockMvc.perform(get("/profiles/getProfile/{login}", "nm12")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"login\":\"facere_a\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_CustomLogin_Anonim"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class ProfileControllerConfig {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SettingUserRepository settingUserRepository = mock(SettingUserRepository.class);

    @Bean
    public PushService pushService() {
        return mock(PushService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return spy(new MainService(dbService, null));
    }

    @Bean
    public ProfileController profileController(MainService mainService, PushService pushService, DBService dbService) {
        return spy(new ProfileController(userRepository, mainService, pushService, settingUserRepository, dbService));
    }
}