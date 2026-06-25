package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.reps.NewsRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

public class NewsControllerTest extends AbstractTestIntegration {
    private final NewsRepository newsRepository;
    private final IDBService dbService;
    private final ISSEService sseService;
    private static final String delNews_Summary = "Удаление новости + Server Sent Events";
    private static final String chNews_Summary = "Изменение новости + Server Sent Events";
    private static final String addNewsPortal_Summary = "Добавление новой новости портала + Server Sent Events";
    private static final String addNewsYO_Summary = "Добавление новой новости учебного центра + Server Sent Events";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    NewsControllerTest(NewsRepository newsRepository, IDBService dbService, ISSEService sseService, NewsController newsController) {
        this.newsRepository = newsRepository;
        this.dbService = dbService;
        this.sseService = sseService;
        this.testController = newsController;
        nameTestedClass = "NewsController";
    }

    /** RU: админ для новостей сайта
     * удаляет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenEmpty_Portal_AdminUser() throws Exception {
        when(dbService.newsById(any())).thenReturn(null);
        getSub().setLvlMore2("Por");

        mockMvc.perform(delete("/news/delNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenEmpty_Portal_AdminUser"));

        verify(sseService, times(0)).sendEventFor(any(), eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser(roles = Roles.HTEACHER)
    void delNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(TEST_UTILS.newsTest.get(1));
        getSub().setLvlMore2("Yo");

        mockMvc.perform(delete("/news/delNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenGood_YO_HTeacher"));

        verify(sseService).sendEventFor(any(), eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1}",
            gson.toJson(answer.getValue()));
    }

    /** RU: админ для новостей сайта
     * удаляет новость и отправляет JSON'ом id новости */
    @Test @Tag("delNews")
    @CustomUser
    void delNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(TEST_UTILS.newsTest.get(1));
        getSub().setLvlMore2("Por");

        mockMvc.perform(delete("/news/delNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(delNews_Summary, "delNews_whenGood_Portal_AdminUser"));

        verify(sseService).sendEventFor(any(), eq("delNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1}",
            gson.toJson(answer.getValue()));
    }

    /** RU: админ для новостей сайта
     * изменяет новость с пустыми данными и отправляет 404-код ответа*/
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenEmpty_Portal_AdminUser() throws Exception {
        getSub().setLvlMore2("Por");

        mockMvc.perform(put("/news/chNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenEmpty_Portal_AdminUser"));

        verify(sseService, times(0)).sendEventFor(any(), eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser(roles = Roles.HTEACHER)
    void chNews_whenGood_YO_HTeacher() throws Exception {
        when(dbService.newsById(1L)).thenReturn(TEST_UTILS.newsTest.get(1));
        getSub().setLvlMore2("Yo");

        mockMvc.perform(put("/news/chNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1,
                "val": "А проект вышел большим...",
                "type": "title"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenGood_YO_HTeacher"));

        verify(sseService).sendEventFor(any(), eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            gson.toJson(answer.getValue()));
    }

    /** RU: админ для новостей сайта
     * изменяет новость и отправляет JSON'ом изменение */
    @Test @Tag("chNews")
    @CustomUser
    void chNews_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.newsById(1L)).thenReturn(TEST_UTILS.newsTest.get(1));
        getSub().setLvlMore2("Por");

        mockMvc.perform(put("/news/chNews")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": 1,
                "val": "А проект вышел большим...",
                "type": "title"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(chNews_Summary, "chNews_whenGood_Portal_AdminUser"));

        verify(sseService).sendEventFor(any(), eq("chNewsC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":1,\"type\":\"title\",\"val\":\"А проект вышел большим...\"}",
            gson.toJson(answer.getValue()));
    }

    /** RU: общий сценарий тестирования */
    private void addNews_run(String methodName, String body, String type, int timesSSE, ResultMatcher status) throws Exception {
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        getSub().setLvlMore2(type);

        mockMvc.perform(post("/news/addNews" + type + "")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status)
            .andDo(defaultSwaggerDocs(type.equals("YO") ? addNewsYO_Summary : addNewsPortal_Summary, methodName));

        verify(sseService, times(timesSSE)).sendEventFor(any(), eq("addNewsC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для школьных новостей
     * создаёт новость и отправляет JSON'ом */
    @Test @Tag("addNewsYO")
    @CustomUser(roles = Roles.HTEACHER)
    void addNewsYO_whenGood_HTeacher() throws Exception {
        User user = dbService.userById(getAuth().getUserId());
        user.getSelecRole().setYO(mock(School.class));

        addNews_run("addNewsYO_whenGood_HTeacher", """
        {
            "title": "День рождения портала!",
            "date": "25.04.2022",
            "text": "Начались первые работы"
        }
        """, "Yo", 1, status().isCreated());
        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"}}",
            gson.toJson(answer.getValue()));
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

        assertEquals("{\"body\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"}}",
            gson.toJson(answer.getValue()));
    }

    private RestDocumentationResultHandler getNews_Docs(String methodName) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправка новостей, портала/школы")
            .description(defaultDescription)
            .pathParameters(parameterWithName("type")
                .type(SimpleType.STRING)
                .description("Нужный тип: Por - портал, Yo - школы")
            )
            .tag("NewsController")
            .requestHeaders(headerWithName(SecurityConfig.SSE_TOKEN_HEADER)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("NewsController/" + methodName, resource(snip.build()));
    }

    /** RU: админ для новостей сайта
     * отправляет JSON'ом пустой список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenEmpty_Portal_Admin() throws Exception {
        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{}"))
            .andDo(getNews_Docs("getNews_whenEmpty_Portal_Admin"));
    }

    /** RU: завуч для школьных новостей
     * отправляет JSON'ом список новостей клиенту */
    @Test @Tag("getNews")
    @CustomUser
    void getNews_whenGood_YO_HTeacher() throws Exception {
        User user = dbService.userById(getAuth().getUserId());
        user.getSelecRole().setYO(mock(School.class));
        when(user.getSelecRole().getYO().getNews())
            .thenReturn(TEST_UTILS.newsTest);

        mockMvc.perform(get("/news/getNews/{type}", "Yo")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
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
            .thenReturn(TEST_UTILS.newsTest);

        mockMvc.perform(get("/news/getNews/{type}", "Por")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"1213\":{\"title\":\"День рождения портала!\",\"date\":\"25.04.2022\",\"text\":\"Начались первые работы\"},\"352\":{\"title\":\"А проект вышел большим...\",\"date\":\"02.12.2022\",\"img_url\":\"/static/media/tuman.jpg\",\"text\":\"Да-да, всё ещё не конец...\"}}"))
            .andDo(getNews_Docs("getNews_whenGood_Portal_AdminUser"));
    }
}