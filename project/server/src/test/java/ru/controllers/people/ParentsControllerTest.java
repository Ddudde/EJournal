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
import ru.controllers.SSEController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.SSE.TypesConnect;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;
import utils.TestUtils;

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
import static utils.TestUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({ParentsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ParentsControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final TestUtils testUtils = new TestUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";
    private MockedStatic theMock;

    @Autowired
    private DBService dbService;

    @Autowired
    private ParentsController parentsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(parentsController)
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
            .tag("ParentsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ParentsController/" + methodName, resource(snip.build()));
    }

    private final String remPep_Summary = "создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";

    @Test @Tag("remPep")
    @CustomAuth
    void remPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(delete("/parents/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * удаляет роль родителя и отправляет JSON'ом инфу */
    @Test @Tag("remPep")
    @CustomUser(roles = Roles.HTEACHER)
    void remPep_whenGood_HTEACHER() throws Exception {
        final Group group = mock(Group.class);
        final List<User> users = new ArrayList<>(usersTest);
        when(dbService.userById(20L)).thenReturn(usersTest.get(0));
        getSub().setLvlGr("20");
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(users);

        mockMvc.perform(delete("/parents/remPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remPep_Summary, "remPep_whenGood_HTEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("remPepC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any()));
        assertEquals("{\"id\":3872}",
            answer.getValue().toString());
        assertEquals(users.size(), 5);
    }

    private final String chPep_Summary = "создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";

    @Test @Tag("chPep")
    @CustomAuth
    void chPep_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/parents/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenEmpty_Anonim"));
    }

    @Test @Tag("chPep")
    @CustomUser(roles = Roles.HTEACHER)
    void chPep_whenGood_HTEACHER() throws Exception {
        when(dbService.userById(20L)).thenReturn(getCloneUsers(usersTest.get(0)));

        mockMvc.perform(patch("/parents/chPep")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                name : "Петров П.А."
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chPep_Summary, "chPep_whenGood_HTEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chPepC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any()));
        assertEquals("{\"id\":3872,\"name\":\"Петров П.А.\"}",
            answer.getValue().toString());
    }

    private final String addPar_Summary = "создаёт пользователя-родителя и сразу прикрепляет к ребёнку + Server Sent Events";

    @Test @Tag("addPar")
    @CustomAuth
    void addPar_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/parents/addPar")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPar_Summary, "addPar_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * создаёт родителя(лей) и отправляет JSON'ом инфу */
    @Test @Tag("addPar")
    @CustomUser(roles = Roles.HTEACHER)
    void addPar_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.userById(20L)).thenReturn(getCloneUsers(usersTest.get(0)));
        getSub().setLvlSch("20");
        when(dbService.schoolById(20L)).thenReturn(sch1);

        mockMvc.perform(post("/parents/addPar")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 20,
                "bod": {
                    name : "Петров П.А.",
                    par : {
                        "id1": {
                            name : "Петров А.А."
                        },
                        "id2": {
                            name : "Петрова А.Б."
                        }
                    }
                }
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPar_Summary, "addPar_whenGood_HTEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addParC"), answer.capture(), eq(TypesConnect.PARENTS), any(), any(), any(), any()));
        assertEquals("{\"id\":3872,\"body\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\",\"par\":{\"null\":{\"name\":\"Петрова А.Б.\"}}}}",
            answer.getValue().toString());
    }

    private final String getParents_Summary = "отправляет список ребёнок-родители группы";

    @Test @Tag("getParents")
    @CustomAuth
    void getParents_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getParents/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getParents_Summary, "getParents_whenEmpty_Anonim"));
    }

    @Test @Tag("getParents")
    @CustomUser(roles = Roles.HTEACHER)
    void getParents_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class, Answers.RETURNS_DEEP_STUBS);
        final Group group = mock(Group.class);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);
        when(sch1.getGroups().contains(any())).thenReturn(true);
        when(dbService.groupById(20L)).thenReturn(group);
        when(group.getKids()).thenReturn(usersTest);

        mockMvc.perform(get("/parents/getParents/{grId}", 20L)
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"bodyP\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\",\"par\":{}},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"par\":{}},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"par\":{}},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"par\":{}},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\",\"par\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}},\"bodyC\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}}"))
            .andDo(defaultSwaggerDocs(getParents_Summary, "getParents_whenGood_HTEACHER"));
    }

    private final String getInfo_Summary = "[start] запускает клиента в раздел Родители и подтверждает клиенту права";
    private final String getInfoForHTeacher_Summary = "[start] отправляет список групп учебного центра и подтверждает клиенту права";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    /** RU: ученик
     * подтверждает клиенту права */
    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.KID)
    void getInfo_whenGood_KID() throws Exception {
        mockMvc.perform(get("/parents/getInfo")
            .header(SecurityConfig.authTokenHeader, bearerToken))
        .andExpect(status().isOk())
        .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_KID"));
    }

    @Test @Tag("getInfoForHTeacher")
    @CustomAuth
    void getInfoForHTeacher_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/parents/getInfoFH")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenEmpty_Anonim"));
    }

    /** RU: завуч
     * отправляет JSON'ом список групп учебного центра и подтверждает клиенту права */
    @Test @Tag("getInfoForHTeacher")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfoForHTeacher_whenGood_HTEACHER() throws Exception {
        final School sch1 = mock(School.class);
        when(dbService.getFirstRole(any()).getYO()).thenReturn(sch1);
        when(sch1.getGroups()).thenReturn(testUtils.groups);

        mockMvc.perform(get("/parents/getInfoFH")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyG\":{\"2323\":\"1А\",\"3456\":\"1Б\",\"4354\":\"1В\"},\"firstG\":2323}"))
            .andDo(defaultSwaggerDocs(getInfoForHTeacher_Summary, "getInfoForHTeacher_whenGood_HTEACHER"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class ParentsControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public ParentsController parentsController() {
        return spy(new ParentsController());
    }
}