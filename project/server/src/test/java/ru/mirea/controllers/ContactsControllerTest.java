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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.security.CustomToken;
import ru.mirea.services.MainService;
import ru.mirea.services.PushService;
import ru.mirea.services.db.DBService;
import ru.mirea.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
public class ContactsControllerTest {

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
    private ContactsController contactsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(contactsController)
            .setMessageConverters(converter)
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    /** RU: админ для контактов сайта
     * клиент не делится нужными данными и получает 404 ответ */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenEmpty_Portal_AdminUser() throws Exception {
        mockMvc.perform(patch("/contacts/chContact/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
        verify(authController, times(0)).sendMessageFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: завуч для контактов школы
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser(roles = "3")
    void chContact_whenGood_YO_HTeacher() throws Exception {
        User user = getSub().getUser();
        getSub().setLvlMore2("Yo");
        when(user.getRoles().containsKey(3L))
            .thenReturn(true);
        when(dbService.getSyst().getContacts())
            .thenReturn(randomUtils.getContactsTest());
        mockMvc.perform(patch("/contacts/chContact/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "p": "mapPr",
            "p1": "text",
            "val": "А проект вышел большим..."
        }
            """)).andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            answer.getValue().toString());
    }

    /** RU: админ для контактов сайта
     * изменяет контакт и отправляет изменение клиенту */
    @Test @Tag("chContact")
    @CustomUser
    void chContact_whenGood_Portal_AdminUser() throws Exception {
        User user = getSub().getUser();
        getSub().setLvlMore2("Por");
        when(user.getRoles().containsKey(4L))
            .thenReturn(true);
        when(dbService.getSyst().getContacts())
            .thenReturn(randomUtils.getContactsTest());
        mockMvc.perform(patch("/contacts/chContact/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "p": "mapPr",
            "p1": "text",
            "val": "А проект вышел большим..."
        }
            """)).andExpect(status().isOk());
        verify(authController).sendMessageFor(eq("chContactC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"val\":\"А проект вышел большим...\",\"p\":\"mapPr\",\"p1\":\"text\"}",
            answer.getValue().toString());
    }

    /** RU: админ для контактов сайта
     * клиент не делится нужными данными и получает 404 ответ */
    @Test @Tag("getContacts")
    @CustomUser
    void getContacts_whenEmpty_Portal_AdminUser() throws Exception {
        when(dbService.getSyst()).thenReturn(null);
        mockMvc.perform(get("/contacts/getContacts/{type}", "Por"))
            .andExpect(status().isNotFound());
    }

    /** RU: завуч для контактов школы
     * отправляет JSON'ом контакты клиенту */
    @Test @Tag("getContacts")
    @CustomUser(roles = "3")
    void getContacts_whenGood_YO_HTeacher() throws Exception {
        User user = getSub().getUser();
        School school = mock(School.class);
        when(user.getRoles().get(any()).getYO()).thenReturn(school);
        when(school.getContacts())
            .thenReturn(randomUtils.contactsTest.get(0));
        mockMvc.perform(get("/contacts/getContacts/{type}", "Yo"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"contact\":\"8 (800) 555 35 37\\n5 (353) 555 00 88\",\"mapPr\":{\"text\":\"Ближайшие станции метро:\\nАлександровский сад, 610 м (Филёвская линия, выход 5)\\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)\",\"imgUrl\":\"/static/media/map.jpg\"}}"));
    }

    /** RU: админ для контактов сайта
     * отправляет JSON'ом контакты клиенту */
    @Test @Tag("getContacts")
    @CustomUser
    void getContacts_whenGood_Portal_AdminUser() throws Exception {
        when(dbService.getSyst().getContacts())
            .thenReturn(randomUtils.contactsTest.get(0));
        mockMvc.perform(get("/contacts/getContacts/{type}", "Por"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"contact\":\"8 (800) 555 35 37\\n5 (353) 555 00 88\",\"mapPr\":{\"text\":\"Ближайшие станции метро:\\nАлександровский сад, 610 м (Филёвская линия, выход 5)\\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)\",\"imgUrl\":\"/static/media/map.jpg\"}}"));
    }
}