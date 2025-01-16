package ru.controllers.people;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.JsonObject;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.AuthController;
import ru.controllers.SSEController;
import ru.data.DAO.auth.User;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.services.MainService;
import ru.services.db.DBService;

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
import static utils.TestUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({AdminsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class AdminsControllerTest {
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
    private AdminsController adminsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(adminsController)
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
            .tag("AdminsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("AdminsController/" + methodName, resource(snip.build()));
    }

    private final String remPep_Summary = "Удаляет у пользователя роль администратора + Server Sent Events";

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(delete("/admins/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * удаляет у пользователя роль администратора и отправляет JSON'ом инфу */
    @Test @Tag("remPep")
    @CustomUser
    void remPep_whenGood_Admin() throws Exception {
        User user = getSub().getUser();
        when(dbService.userById(20L)).thenReturn(user);
        mockMvc.perform(delete("/admins/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764}",
            answer.getValue().toString());
    }

    private final String chPep_Summary = "Изменяет фамилию пользователя + Server Sent Events";

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(patch("/admins/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * изменяет фамилию пользователя и отправляет JSON'ом инфу */
    @Test @Tag("chPep")
    @CustomUser
    void chPep_whenGood_Admin() throws Exception {
        User user = getSub().getUser();
        when(dbService.userById(20L)).thenReturn(user);
        mockMvc.perform(patch("/admins/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Дрыздов А.А.",
                "id": 20
            }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764,\"name\":\"Дрыздов А.А.\"}",
            answer.getValue().toString());
    }

    private final String addPep_Summary = "Cоздаёт пользователя-администратора + Server Sent Events";

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        mockMvc.perform(post("/admins/addPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
        theMock.verify(() -> SSEController.sendEventFor(eq("addPepC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: админ
     * добавляет нового админа и отправляет JSON'ом инфу */
    @Test @Tag("addPep")
    @CustomUser
    void addPep_whenGood_Admin() throws Exception {
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        mockMvc.perform(post("/admins/addPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Дрыздов А.А."
            }
            """))
            .andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_Admin"));
        theMock.verify(() -> SSEController.sendEventFor(eq("addPepC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"name\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    private final String getAdmins_Summary = "[start] Отправляет список администраторов";

    /** RU: аноним
     * не находит систему и отправляет 404 код-ответ */
    @Test @Tag("getAdmins")
    @CustomAuth
    void getAdmins_whenEmpty_Anonim() throws Exception {
        when(dbService.getSyst()).thenReturn(null);
        mockMvc.perform(get("/admins/getAdmins")
                .header(SecurityConfig.authTokenHeader, bearerToken))
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
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getAdmins_Summary, "getAdmins_whenGood_Admin"));
    }

}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class AdminsControllerConfig {

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
    public AdminsController adminsController() {
        return spy(new AdminsController());
    }
}