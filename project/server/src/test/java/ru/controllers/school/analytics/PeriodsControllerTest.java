package ru.controllers.school.analytics;

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
import ru.data.DAO.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;
import utils.TestUtils;

import javax.servlet.ServletException;
import java.util.ArrayList;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;
import static utils.TestUtils.getSub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({PeriodsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PeriodsControllerTest {
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
    private PeriodsController periodsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(periodsController)
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
            .tag("PeriodsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("PeriodsController/" + methodName, resource(snip.build()));
    }

    private final String addPer_Summary = "[start] отправляет данные о расписании периодов обучения учебного центра";

    @Test @Tag("addPer")
    @CustomAuth
    void addPer_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/periods/addPer")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPer_Summary, "addPer_whenEmpty_Anonim"));
    }

    @Test @Tag("addPer")
    @CustomUser(roles = Roles.HTEACHER)
    void addPer_whenGood_HTEACHER() throws Exception {
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getPeriods()).thenReturn(new ArrayList<>(testUtils.periods));
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(post("/periods/addPer")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "perN": "12.11.23",
                "perK": "29.12.23",
                "name": "II четверть"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPer_Summary, "addPer_whenGood_HTEACHER"));

        theMock.verify(() -> SSEController.sendEventFor(eq("addPerC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":null,\"body\":{\"name\":\"II четверть\",\"perN\":\"12.11.23\",\"perK\":\"29.12.23\"}}",
            answer.getValue().toString());
    }

    private final String getInfo_Summary = "[start] отправляет данные о расписании периодов обучения учебного центра";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/periods/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfo_whenGood_HTEACHER() throws Exception {
        final User user = getSub().getUser();
        final School sch1 = mock(School.class);
        when(sch1.getPeriods()).thenReturn(testUtils.periods);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/periods/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyP\":{\"0\":{\"name\":\"I четверть\",\"perN\":\"01.09.23\",\"perK\":\"03.11.23\"},\"1\":{\"name\":\"II четверть\",\"perN\":\"12.11.23\",\"perK\":\"29.12.23\"},\"2\":{\"name\":\"III четверть\",\"perN\":\"12.01.24\",\"perK\":\"29.03.24\"},\"3\":{\"name\":\"IV четверть\",\"perN\":\"01.04.24\",\"perK\":\"30.08.24\"}}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_HTEACHER"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class PeriodsControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public PeriodsController periodsController() {
        return spy(new PeriodsController());
    }
}