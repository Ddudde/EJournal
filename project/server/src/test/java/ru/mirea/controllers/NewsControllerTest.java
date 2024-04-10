package ru.mirea.controllers;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.security.CustomToken;
import ru.mirea.services.MainService;
import ru.mirea.services.PushService;
import ru.mirea.services.db.DBService;
import ru.mirea.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.mirea.Main.datas;

@ExtendWith({SpringExtension.class})
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

    private static RandomUtils randomUtils;

    private static final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();

    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @BeforeAll
    static void beforeAll() throws ServletException {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        randomUtils = new RandomUtils();
        authInjector.afterPropertiesSet();
    }

    @BeforeEach
    void setUp() {
        mainService.postConstruct();
        mockMvc = MockMvcBuilders.standaloneSetup(newsController)
            .setMessageConverters(converter)
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }



    /** RU: админ для новостей сайта
     * удаляет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenEmpty_Portal_AdminUser() throws Exception {
        when(dbService.newsById(any())).thenReturn(null);
        when(getSub().getUser().getRoles().containsKey(4L))
            .thenReturn(true);
        getSub().setLvlMore2("Por");
        mockMvc.perform(delete("/news/delNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
        verify(authController, times(0)).sendMessageFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser(roles = "3")
    void delNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        when(getSub().getUser().getRoles().containsKey(3L))
            .thenReturn(true);
        getSub().setLvlMore2("Yo");
        mockMvc.perform(delete("/news/delNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1
        }
            """)).andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        when(getSub().getUser().getRoles().containsKey(4L))
            .thenReturn(true);
        getSub().setLvlMore2("Por");
        mockMvc.perform(delete("/news/delNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1
        }
            """)).andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * изменяет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenEmpty_Portal_AdminUser() throws Exception {
        when(getSub().getUser().getRoles().containsKey(4L))
            .thenReturn(true);
        getSub().setLvlMore2("Por");
        mockMvc.perform(patch("/news/chNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
        verify(authController, times(0)).sendMessageFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser(roles = "3")
    void chNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        when(getSub().getUser().getRoles().containsKey(3L))
            .thenReturn(true);
        getSub().setLvlMore2("Yo");
        mockMvc.perform(patch("/news/chNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1,
            "val": "А проект вышел большим...",
            "type": "title"
        }
        """))
            .andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(randomUtils.newsTest.get(1));
        when(getSub().getUser().getRoles().containsKey(4L))
            .thenReturn(true);
        getSub().setLvlMore2("Por");
        mockMvc.perform(patch("/news/chNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": 1,
            "val": "А проект вышел большим...",
            "type": "title"
        }
            """)).andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            answer.getValue().toString());
    }

    /** RU: общий сценарий тестирования */
    private void addNews_run(String body, String type, long role, int timesSSE, ResultMatcher status) throws Exception {
        when(dbService.getNewsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(getSub().getUser().getRoles().containsKey(role))
            .thenReturn(true);
        getSub().setLvlMore2(type);
        mockMvc.perform(post("/news/addNews/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status);
        verify(authController, times(timesSSE)).sendMessageFor(eq("addNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNews")
    @CustomUser(roles = "3")
    void addNews_whenGood_YO_HTeacher() throws Exception {
        addNews_run("""
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Yo", 3L, 1, status().isCreated());
        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"img_url\":null,\"text\":\"Начались первые работы\"},\"id\":null}",
                answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * создаёт пустую новость и отправляет 404-код ответа */
    @Test @Tag("addNews")
    @CustomUser
    void addNews_whenEmpty_Portal_AdminUser() throws Exception {
        addNews_run("{}", "Por", 4L, 0, status().isNotFound());
    }

    /** RU: админ для новостей сайта
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNews")
    @CustomUser
    void addNews_whenGood_Portal_AdminUser() throws Exception {
        addNews_run("""
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Por", 4L, 1, status().isCreated());
        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"img_url\":null,\"text\":\"Начались первые работы\"},\"id\":null}",
            answer.getValue().toString());
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом пустой список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenEmpty_Portal_Admin() throws Exception {
        mockMvc.perform(get("/news/getNews/{type}", "Por"))
            .andExpect(status().isOk())
            .andExpect(content().json("{}"));
    }

    /** RU: завуч для школьных новостей
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_YO_HTeacher() throws Exception {
        when(getSub().getUser().getSelecRole().getYO()
            .getNews()).thenReturn(randomUtils.newsTest);
        mockMvc.perform(get("/news/getNews/{type}", "Yo"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_Portal_AdminUser() throws Exception {
        when(datas.getDbService().getSyst().getNews())
            .thenReturn(randomUtils.newsTest);
        mockMvc.perform(get("/news/getNews/{type}", "Por"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"));
    }
}