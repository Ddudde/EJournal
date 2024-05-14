package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import com.google.gson.JsonObject;
import config.CustomUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;
import ru.data.models.auth.User;
import ru.data.models.school.School;
import ru.security.CustomToken;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.Main.datas;
import static utils.RandomUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class NewsControllerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IniDBService iniDBService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DBService dbService;

    @Mock
    private PushService pushService;

    @InjectMocks
    private MainService mainService;

    @Mock
    private AuthController authController;

    @InjectMocks
    private NewsController newsController;

    @Captor
    private ArgumentCaptor<JsonObject> answer;

    private MockMvc mockMvc;

    private final RandomUtils randomUtils = new RandomUtils();

    private static final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();

    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @BeforeAll
    static void beforeAll() throws ServletException {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        authInjector.afterPropertiesSet();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mainService.postConstruct();
        mockMvc = MockMvcBuilders.standaloneSetup(newsController)
            .setMessageConverters(converter)
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
            .tag("NewsController")
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
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
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(delNews_Summary, "delNews_whenEmpty_Portal_AdminUser"));
        verify(authController, times(0)).sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser(roles = "3")
    void delNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        getSub().setLvlMore2("Yo");
        mockMvc.perform(delete("/news/delNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1
        }
            """)).andExpect(status().isOk())
            .andDo(default_Docs(delNews_Summary, "delNews_whenGood_YO_HTeacher"));
        verify(authController).sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        getSub().setLvlMore2("Por");
        mockMvc.perform(delete("/news/delNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1
        }
            """)).andExpect(status().isOk())
            .andDo(default_Docs(delNews_Summary, "delNews_whenGood_Portal_AdminUser"));
        verify(authController).sendEventFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
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
        mockMvc.perform(patch("/news/chNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(chNews_Summary, "chNews_whenEmpty_Portal_AdminUser"));
        verify(authController, times(0)).sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser(roles = "3")
    void chNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        getSub().setLvlMore2("Yo");
        mockMvc.perform(patch("/news/chNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1,
            "val": "А проект вышел большим...",
            "type": "title"
        }
        """))
            .andExpect(status().isOk())
            .andDo(default_Docs(chNews_Summary, "chNews_whenGood_YO_HTeacher"));
        verify(authController).sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        getSub().setLvlMore2("Por");
        mockMvc.perform(patch("/news/chNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1,
            "val": "А проект вышел большим...",
            "type": "title"
        }
            """)).andExpect(status().isOk())
            .andDo(default_Docs(chNews_Summary, "chNews_whenGood_Portal_AdminUser"));
        verify(authController).sendEventFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    private final String addNews_Summary = "Добавление новой новости + Server Sent Events";

    /** RU: общий сценарий тестирования */
    private void addNews_run(String methodName, String body, String type, int timesSSE, ResultMatcher status) throws Exception {
        when(dbService.getNewsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        getSub().setLvlMore2(type);
        mockMvc.perform(post("/news/addNews/")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status)
            .andDo(default_Docs(addNews_Summary, methodName));
        verify(authController, times(timesSSE)).sendEventFor(eq("addNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNews")
    @CustomUser(roles = "3")
    void addNews_whenGood_YO_HTeacher() throws Exception {
        addNews_run("addNews_whenGood_YO_HTeacher", """
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Yo", 1, status().isCreated());
        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"img_url\":null,\"text\":\"Начались первые работы\"},\"id\":null}",
                answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * создаёт пустую новость и отправляет 404-код ответа */
    @Test @Tag("addNews")
    @CustomUser
    void addNews_whenEmpty_Portal_AdminUser() throws Exception {
        addNews_run("addNews_whenEmpty_Portal_AdminUser", "{}", "Por", 0, status().isNotFound());
    }

    /** RU: админ для новостей сайта
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNews")
    @CustomUser
    void addNews_whenGood_Portal_AdminUser() throws Exception {
        addNews_run("addNews_whenGood_Portal_AdminUser", """
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
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("NewsController/" + methodName, resource(snip.build()));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом пустой список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenEmpty_Portal_Admin() throws Exception {
        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
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
            .thenReturn(randomUtils.newsTest);
        mockMvc.perform(get("/news/getNews/{type}", "Yo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"))
            .andDo(getNews_Docs("getNews_whenGood_YO_HTeacher"));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_Portal_AdminUser() throws Exception {
        when(datas.getDbService().getSyst().getNews())
            .thenReturn(randomUtils.newsTest);
        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"))
            .andDo(getNews_Docs("getNews_whenGood_Portal_AdminUser"));
    }
}