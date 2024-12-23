package ru.controllers.people;

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
import ru.data.models.auth.Role;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;

import javax.servlet.ServletException;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.Main.datas;
import static utils.RandomUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({HTeachersControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HTeachersControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private HTeachersController hTeachersController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(hTeachersController)
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
            .tag("HTeachersController")
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("HTeachersController/" + methodName, resource(snip.build()));
    }

    private final String remGroup_Summary = "Удаляет группу + Server Sent Events";

    @Test @Tag("remGroup")
    @CustomAuth
    void remGroup_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("remGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void remGroup_whenGood_Hteacher() throws Exception {
        final School school = mock(School.class);
        final User user = getSub().getUser();
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "grId": 20
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenGood_Hteacher"));

        verify(authController).sendEventFor(eq("remGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20}",
            answer.getValue().toString());
    }

    private final String addGroup_Summary = "Создаёт группу + Server Sent Events";

    @Test @Tag("addGroup")
    @CustomAuth
    void addGroup_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("addGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void addGroup_whenGood_Hteacher() throws Exception {
        final School school = mock(School.class);
        final User user = getSub().getUser();
        user.getRoles().get(Roles.HTEACHER).setYO(school);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "name": "31В"
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenGood_Hteacher"));

        verify(authController).sendEventFor(eq("addGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":null,\"name\":\"31В\"}",
            answer.getValue().toString());
    }

    private final String chGroup_Summary = "Изменяет название группы + Server Sent Events";

    @Test @Tag("chGroup")
    @CustomAuth
    void chGroup_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("chGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void chGroup_whenGood_Hteacher() throws Exception {
        final School school = mock(School.class);
        final User user = getSub().getUser();
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "grId": 20,
            "name": "31В"
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenGood_Hteacher"));

        verify(authController).sendEventFor(eq("chGroupC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20,\"name\":\"31В\"}",
            answer.getValue().toString());
    }

    private final String chPep_Summary = "Изменяет фамилию пользователя + Server Sent Events";

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);
        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser
    void chPep_whenGood_Admin() throws Exception {
        final User user = getSub().getUser();
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 20,
            "name": "Дрыздов А.А."
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_Admin"));

        verify(authController).sendEventFor(eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"id1\":20,\"name\":\"Дрыздов А.А.\"}",
            answer.getValue().toString());
    }

    private final String remPep_Summary = "Удаляет у пользователя роль завуча + Server Sent Events";

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);
        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser
    void remPep_whenGood_Admin() throws Exception {
        final User user = getSub().getUser();
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 20
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_Admin"));

        verify(authController).sendEventFor(eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"id1\":20}",
            answer.getValue().toString());
    }

    private final String addPep_Summary = "Создаёт пользователя-завуча + Server Sent Events";

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
    }

    @Test @Tag("addPep")
    @CustomUser
    void addPep_whenGood_Admin() throws Exception {
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.schoolById(any()).getId()).thenReturn(20L);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "yo": 20,
            "name": "Дрыздов А.А."
        }
            """))
            .andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_Admin"));

        verify(authController).sendEventFor(eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        verify(authController).sendEventFor(eq("addInfoL2C"), any(), any(), any(), any(), any(), any());
        assertEquals("{\"id1\":20,\"id\":null,\"body\":{\"name\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    private final String chSch_Summary = "Изменение имени учебного центра администратором портала";

    @Test @Tag("chSch")
    @CustomAuth
    void chSch_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenEmpty_Anonim"));
    }

    @Test @Tag("chSch")
    @CustomUser
    void chSch_whenGood_Admin() throws Exception {
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "schId": 20,
            "name": "Гимназия ? 4"
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenGood_Admin"));

        verify(authController).sendEventFor(eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20,\"name\":\"Гимназия ? 4\"}",
            answer.getValue().toString());
    }

    private final String addSch_Summary = "Добавление учебного центра администратором портала";

    @Test @Tag("addSch")
    @CustomAuth
    void addSch_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenEmpty_Anonim"));
    }

    @Test @Tag("addSch")
    @CustomUser
    void addSch_whenGood_Admin() throws Exception {
        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "name": "Гимназия ? 4"
        }
            """))
            .andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenGood_Admin"));

        verify(authController).sendEventFor(eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":null,\"body\":{\"name\":\"Гимназия ? 4\"}}",
            answer.getValue().toString());
    }

    private final String remSch_Summary = "Удаление учебного центра администратором портала";

    @Test @Tag("remSch")
    @CustomAuth
    void remSch_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenEmpty_Anonim"));
    }

    @Test @Tag("remSch")
    @CustomUser
    void remSch_whenGood_Admin() throws Exception {
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "schId": 20
        }
            """))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenGood_Admin"));

        verify(authController).sendEventFor(eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":20}",
            answer.getValue().toString());
    }

    private final String getInfo_Summary = "[start] отправка список завучей учебного центра";
    private final String getInfoForAdmins_Summary = "[start] отправка список завучей учебного центра для администраторов";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfo_whenGood_HTEACHER() throws Exception {
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_HTEACHER"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomAuth
    void getInfoForAdmins_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomUser
    void getInfoForAdmins_whenGood_Admin() throws Exception {
        final School sch1 = mock(School.class);
        final School sch2 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        when(sch2.getHteachers()).thenReturn(usersTest);
        when(datas.getDbService().getSchools()).thenReturn(List.of(sch1, sch2));

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"0\":{\"pep\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}}}"))
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenGood_Admin"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class HTeachersControllerConfig {

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
    public HTeachersController hTeachersController(AuthController authController) {
        return spy(new HTeachersController(authController));
    }
}