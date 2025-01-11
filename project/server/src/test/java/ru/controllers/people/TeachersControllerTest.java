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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.controllers.AuthController;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;

import javax.servlet.ServletException;
import java.util.ArrayList;
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
import static utils.RandomUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({TeachersControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TeachersControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private DBService dbService;

    @Autowired
    private AuthController authController;

    @Autowired
    private TeachersController teachersController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(teachersController)
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
            .tag("TeachersController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("TeachersController/" + methodName, resource(snip.build()));
    }

    private final String remPep_Summary = "Удаление роли преподавателя";
    
    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final User user = getSub().getUser();
        final Group group = mock(Group.class);
        getSub().setLvlGr("20");
        when(dbService.userById(20L)).thenReturn(user);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(new ArrayList<>(usersTest));

        mockMvc.perform(delete("/teachers/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        verify(authController).sendEventFor(eq("remPepC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764}",
            answer.getValue().toString());
    }

    private final String chPep_Summary = "Изменяет ФИО преподавателю учебного центра.";

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        final User user = getSub().getUser();
        when(dbService.userById(20L)).thenReturn(user);

        mockMvc.perform(patch("/teachers/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якуш А.О."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        verify(authController).sendEventFor(eq("chPepC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"name\":\"Якуш А.О.\"}",
            answer.getValue().toString());
    }

    private final String addTea_Summary = "Cоздаёт нового учителя для учебного центра";

    @Test @Tag("addTea")
    @CustomAuth
    void addTea_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/teachers/addTea")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addTea_Summary, "addTea_whenEmpty_Anonim"));
    }

    @Test @Tag("addTea")
    @CustomUser(roles = Roles.HTEACHER)
    void addTea_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        getSub().setLvlSch("20");
        when(dbService.schoolById(20L)).thenReturn(sch1);

        mockMvc.perform(post("/teachers/addTea")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "20",
                "name": "Якушева А.О."
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addTea_Summary, "addTea_whenGood_HTEACHER"));

        verify(authController).sendEventFor(eq("addTeaC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":null,\"name\":\"Якушева А.О.\"}",
            answer.getValue().toString());
    }

    private final String getTeachers_Summary = "[start] отправка списка учителей учебного центра";

    @Test @Tag("getTeachers")
    @CustomAuth
    void getTeachers_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getTeachers_Summary, "getTeachers_whenEmpty_Anonim"));
    }

    @Test @Tag("getTeachers")
    @CustomUser(roles = Roles.HTEACHER)
    void getTeachers_whenGood_HTEACHER() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getId()).thenReturn(20L);
        when(sch1.getTeachers()).thenReturn(usersTest);
        user.getSelecRole().setYO(sch1);
        prepareTeachersByLessons();

        mockMvc.perform(get("/teachers/getTeachers")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(statusCode)
            .andExpect(content().string("{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Англ. Яз\",\"tea\":{\"22\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"23\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"24\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"1\":{\"name\":\"Математика\",\"tea\":{\"25\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}}}"))
            .andDo(defaultSwaggerDocs(getTeachers_Summary, "getTeachers_whenGood_HTEACHER"));
    }

    /** RU: создаём данные для списка дисциплина-учителя */
    private void prepareTeachersByLessons() {
        final List<Object[]> lessons = List.of(
            new Object[]{"Англ. Яз", 22L},
            new Object[]{"Англ. Яз", 23L},
            new Object[]{"Англ. Яз", 24L},
            new Object[]{"Математика", 25L}
        );
        when(dbService.userById(22L)).thenReturn(usersTest.get(0));
        when(dbService.userById(23L)).thenReturn(usersTest.get(1));
        when(dbService.userById(24L)).thenReturn(usersTest.get(2));
        when(dbService.userById(25L)).thenReturn(usersTest.get(3));
        when(dbService.getLessonRepository()
            .uniqTeachersLBySchool(20L)).thenReturn(lessons);
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class TeachersControllerConfig {

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
    public TeachersController teachersController(AuthController authController) {
        return spy(new TeachersController(authController));
    }
}