package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import com.google.gson.JsonObject;
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
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.school.SchoolRepository;
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
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;
import static utils.TestUtils.getSub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({NewsControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NewsControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final TestUtils testUtils = new TestUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";
    private MockedStatic theMock;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private DBService dbService;

    @Autowired
    private NewsController newsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(newsController)
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
            .tag("NewsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("NewsController/" + methodName, resource(snip.build()));
    }

    private final String delNews_Summary = "Удаление новости + Server Sent Events";

    /** RU: админ для новостей сайта
     * удаляет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenEmpty_Portal_AdminUser() throws Exception {
        when(dbService.newsById(any())).thenReturn(null);
        getSub().setLvlMore2("Por");

        mockMvc.perform(delete("/news/delNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenEmpty_Portal_AdminUser"));

        theMock.verify(() -> SSEController.sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: завуч для школьных новостей
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser(roles = Roles.HTEACHER)
    void delNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(testUtils.newsTest.get(1));
        getSub().setLvlMore2("Yo");

        mockMvc.perform(delete("/news/delNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenGood_YO_HTeacher"));

        theMock.verify(() -> SSEController.sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":1}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(testUtils.newsTest.get(1));
        getSub().setLvlMore2("Por");

        mockMvc.perform(delete("/news/delNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenGood_Portal_AdminUser"));

        theMock.verify(() -> SSEController.sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":1}",
            answer.getValue().toString());
    }

    private final String chNews_Summary = "Изменение новости + Server Sent Events";

    /** RU: админ для новостей сайта
     * изменяет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenEmpty_Portal_AdminUser() throws Exception {
        getSub().setLvlMore2("Por");

        mockMvc.perform(put("/news/chNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenEmpty_Portal_AdminUser"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any()),
            times(0));
    }

    /** RU: завуч для школьных новостей
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser(roles = Roles.HTEACHER)
    void chNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(testUtils.newsTest.get(1));
        getSub().setLvlMore2("Yo");

        mockMvc.perform(put("/news/chNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1,
                "val": "А проект вышел большим...",
                "type": "title"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenGood_YO_HTeacher"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(testUtils.newsTest.get(1));
        getSub().setLvlMore2("Por");

        mockMvc.perform(put("/news/chNews/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1,
                "val": "А проект вышел большим...",
                "type": "title"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenGood_Portal_AdminUser"));

        theMock.verify(() -> SSEController.sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any()));
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    private final String addNewsPortal_Summary = "Добавление новой новости портала + Server Sent Events";
    private final String addNewsYO_Summary = "Добавление новой новости учебного центра + Server Sent Events";

    /** RU: общий сценарий тестирования */
    private void addNews_run(String methodName, String body, String type, int timesSSE, ResultMatcher status) throws Exception {
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        getSub().setLvlMore2(type);

        mockMvc.perform(post("/news/addNews" + type + "/")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status)
            .andDo(defaultSwaggerDocs(type.equals("YO") ? addNewsYO_Summary : addNewsPortal_Summary, methodName));

        theMock.verify(() -> SSEController.sendEventFor(eq("addNewsC"), answer.capture(), any(), any(), any(), any(), any()),
            times(timesSSE));
    }

    /** RU: завуч для школьных новостей
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNewsYO")
    @CustomUser(roles = Roles.HTEACHER)
    void addNewsYO_whenGood_HTeacher() throws Exception {
        User user = getSub().getUser();
        user.getSelecRole().setYO(mock(School.class));

        addNews_run("addNewsYO_whenGood_HTeacher", """
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Yo", 1, status().isCreated());
        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"img_url\":null,\"text\":\"Начались первые работы\"},\"id\":null}",
            answer.getValue().toString());
    }

    /** RU: завуч для школьных новостей
     * создаёт пустую новость и отправляет 404-код ответа */
    @Test @Tag("addNewsYO")
    @CustomUser(roles = Roles.HTEACHER)
    void addNewsYO_whenEmpty_HTeacher() throws Exception {
        addNews_run("addNewsYO_whenEmpty_HTeacher", "{}", "Yo", 0, status().isNotFound());
    }

    /** RU: админ для новостей сайта
     * создаёт пустую новость и отправляет 404-код ответа */
    @Test @Tag("addNewsPortal")
    @CustomUser
    void addNewsPortal_whenEmpty_AdminUser() throws Exception {
        addNews_run("addNewsPortal_whenEmpty_AdminUser", "{}", "Por", 0, status().isNotFound());
    }

    /** RU: админ для новостей сайта
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNewsPortal")
    @CustomUser
    void addNewsPortal_whenGood_AdminUser() throws Exception {
        addNews_run("addNewsPortal_whenGood_AdminUser", """
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Por", 1, status().isCreated());

        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"img_url\":null,\"text\":\"Начались первые работы\"},\"id\":null}",
            answer.getValue().toString());
    }

    private RestDocumentationResultHandler getNews_Docs(String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправка новостей, портала/школы")
            .description(defaultDescription)
            .pathParameters(parameterWithName("type")
                .type(SimpleType.STRING)
                .description("Нужный тип: Por - портал, Yo - школы")
            )
            .tag("NewsController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("NewsController/" + methodName, resource(snip.build()));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом пустой список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenEmpty_Portal_Admin() throws Exception {
        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{}"))
            .andDo(getNews_Docs("getNews_whenEmpty_Portal_Admin"));
    }

    /** RU: завуч для школьных новостей
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_YO_HTeacher() throws Exception {
        User user = getSub().getUser();
        user.getSelecRole().setYO(mock(School.class));
        when(user.getSelecRole().getYO().getNews())
            .thenReturn(testUtils.newsTest);

        mockMvc.perform(get("/news/getNews/{type}", "Yo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"))
            .andDo(getNews_Docs("getNews_whenGood_YO_HTeacher"));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.getSyst().getNews())
            .thenReturn(testUtils.newsTest);

        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"))
            .andDo(getNews_Docs("getNews_whenGood_Portal_AdminUser"));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class NewsControllerConfig {

    @Bean
    public SchoolRepository schoolRepository() {
        return mock(SchoolRepository.class);
    }

    @Bean
    public NewsRepository newsRepository() {
        return mock(NewsRepository.class);
    }

    @Bean
    public SystRepository systRepository() {
        return mock(SystRepository.class);
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
    public MainService mainService(DBService dbService, PushService pushService) {
        return new MainService(pushService, dbService, null);
    }

    @Bean
    public NewsController newsController(DBService dbService, PushService pushService,
         SystRepository systRepository, NewsRepository newsRepository, SchoolRepository schoolRepository) {
        return spy(new NewsController(dbService, pushService, systRepository, newsRepository, schoolRepository));
    }
}