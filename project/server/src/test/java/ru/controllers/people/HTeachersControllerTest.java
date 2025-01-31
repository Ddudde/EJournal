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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.SSEController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.data.reps.school.SchoolRepository;
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
import static utils.TestUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({HTeachersControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HTeachersControllerTest {
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
    private RoleRepository roleRepository;

    @Autowired
    private HTeachersController hTeachersController;

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
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("HTeachersController/" + methodName, resource(snip.build()));
    }

    private final String remGroup_Summary = "Удаляет группу + Server Sent Events";

    @Test @Tag("remGroup")
    @CustomAuth
    void remGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("remGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void remGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(delete("/hteachers/remGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "grId": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remGroup_Summary, "remGroup_whenGood_Hteacher"));

        theMock.verify(() -> SSEController.sendEventFor(eq("remGroupC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":20}",
            answer.getValue().toString());
    }

    private final String addGroup_Summary = "Создаёт группу + Server Sent Events";

    @Test @Tag("addGroup")
    @CustomAuth
    void addGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("addGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void addGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        final User user = dbService.userById(getSub().getUserId());
        user.getRoles().get(Roles.HTEACHER).setYO(school);

        mockMvc.perform(post("/hteachers/addGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "31В"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addGroup_Summary, "addGroup_whenGood_Hteacher"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addGroupC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"name\":\"31В\"}",
            answer.getValue().toString());
    }

    private final String chGroup_Summary = "Изменяет название группы + Server Sent Events";

    @Test @Tag("chGroup")
    @CustomAuth
    void chGroup_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenEmpty_Anonim"));
    }

    @Test @Tag("chGroup")
    @CustomUser(roles = Roles.HTEACHER)
    void chGroup_whenGood_Hteacher() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        final User user = dbService.userById(getSub().getUserId());
        final Group group = mock(Group.class);
        user.getRoles().get(Roles.HTEACHER).setYO(school);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getId()).thenReturn(20L);

        mockMvc.perform(patch("/hteachers/chGroup")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "grId": 20,
                "name": "31В"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chGroup_Summary, "chGroup_whenGood_Hteacher"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chGroupC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":20,\"name\":\"31В\"}",
            answer.getValue().toString());
    }

    private final String chPep_Summary = "Изменяет фамилию пользователя + Server Sent Events";

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);
        
        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser
    void chPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getSub().getUserId());
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(patch("/hteachers/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                "name": "Дрыздов А.А."
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764,\"id1\":20,\"name\":\"Дрыздов А.А.\"}",
            answer.getValue().toString());
    }

    private final String remPep_Summary = "Удаляет у пользователя роль завуча + Server Sent Events";

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);
        
        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser
    void remPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getSub().getUserId());
        final School school = mock(School.class);
        final Role role = mock(Role.class);
        user.getRoles().put(Roles.HTEACHER, role);
        when(dbService.userById(20L)).thenReturn(user);
        when(school.getId()).thenReturn(20L);
        when(role.getYO()).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":9764,\"id1\":20}",
            answer.getValue().toString());
    }

    private final String addPep_Summary = "Создаёт пользователя-завуча + Server Sent Events";

    @Test @Tag("addPep")
    @CustomAuth
    void addPep_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenEmpty_Anonim"));
    }

    @Test @Tag("addPep")
    @CustomUser
    void addPep_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isCreated();
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.schoolById(any()).getId()).thenReturn(20L);

        mockMvc.perform(post("/hteachers/addPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "yo": 20,
                "name": "Дрыздов А.А."
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addPep_Summary, "addPep_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        theMock.verify(() -> SSEController.sendEventFor(eq("addInfoL2C"), any(), any(), any(), any(), any(), any()));
        assertEquals("{\"id1\":20,\"id\":null,\"body\":{\"name\":\"Дрыздов А.А.\"}}",
            answer.getValue().toString());
    }

    private final String chSch_Summary = "Изменение имени учебного центра администратором портала";

    @Test @Tag("chSch")
    @CustomAuth
    void chSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenEmpty_Anonim"));
    }

    @Test @Tag("chSch")
    @CustomUser
    void chSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);

        mockMvc.perform(patch("/hteachers/chSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "schId": 20,
                "name": "Гимназия ? 4"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSch_Summary, "chSch_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":20,\"name\":\"Гимназия ? 4\"}",
            answer.getValue().toString());
    }

    private final String addSch_Summary = "Добавление учебного центра администратором портала";

    @Test @Tag("addSch")
    @CustomAuth
    void addSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenEmpty_Anonim"));
    }

    @Test @Tag("addSch")
    @CustomUser
    void addSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isCreated();
        
        mockMvc.perform(post("/hteachers/addSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "name": "Гимназия ? 4"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addSch_Summary, "addSch_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"name\":\"Гимназия ? 4\"}}",
            answer.getValue().toString());
    }

    private final String remSch_Summary = "Удаление учебного центра администратором портала";

    @Test @Tag("remSch")
    @CustomAuth
    void remSch_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenEmpty_Anonim"));
    }

    @Test @Tag("remSch")
    @CustomUser
    void remSch_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School school = mock(School.class);
        when(dbService.schoolById(20L)).thenReturn(school);

        mockMvc.perform(delete("/hteachers/remSch")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "schId": 20
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remSch_Summary, "remSch_whenGood_Admin"));

        theMock.verify(() -> SSEController.sendEventFor(eq("remInfoL1C"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":20}",
            answer.getValue().toString());
    }

    private final String getInfo_Summary = "[start] отправка список завучей учебного центра";
    private final String getInfoForAdmins_Summary = "[start] отправка список завучей учебного центра для администраторов";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfo_whenGood_HTEACHER() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = dbService.userById(getSub().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/hteachers/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_HTEACHER"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomAuth
    void getInfoForAdmins_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfoForAdmins")
    @CustomUser
    void getInfoForAdmins_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final School sch1 = mock(School.class);
        final School sch2 = mock(School.class);
        when(sch1.getHteachers()).thenReturn(usersTest);
        when(sch2.getHteachers()).thenReturn(usersTest);
        when(dbService.getSchools()).thenReturn(List.of(sch1, sch2));

        mockMvc.perform(get("/hteachers/getInfoFA")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().json("{\"0\":{\"pep\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}}}"))
            .andDo(defaultSwaggerDocs(getInfoForAdmins_Summary, "getInfoForAdmins_whenGood_Admin"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class HTeachersControllerConfig {
    private final SchoolRepository schoolRepository = mock(SchoolRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean
    public RoleRepository roleRepository() {
        return mock(RoleRepository.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(dbService, null);
    }

    @Bean
    public HTeachersController hTeachersController(RoleRepository roleRepository, DBService dbService,
       MainService mainService) {
        return spy(new HTeachersController(schoolRepository, groupRepository, dbService, mainService, userRepository,
            roleRepository));
    }
}