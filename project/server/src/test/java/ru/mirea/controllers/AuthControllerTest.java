package ru.mirea.controllers;

import config.CustomUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.models.auth.User;
import ru.mirea.security.CustomToken;
import ru.mirea.services.EmailService;
import ru.mirea.services.MainService;
import ru.mirea.services.PushService;
import ru.mirea.services.db.DBService;
import ru.mirea.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
public class AuthControllerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IniDBService iniDBService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DBService dbService;

    @Mock
    private EmailService emailService;

    @Mock
    private PushService pushService;

    @InjectMocks
    private MainService mainService;

    @InjectMocks
    private AuthController authController;

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
        Main.datas = mainService;
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setMessageConverters(converter)
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    /** RU: старая авторизованная подписка пользователя существует
     * новая заменяется старой */
    @Test @Tag("sendMessageFor")
    @CustomUser
    void sendMessageFor_whenGood_AdminUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        mainService.subscriptions.put(uuid, mock(Subscriber.class, Answers.RETURNS_DEEP_STUBS));
        mockMvc.perform(patch("/auth/infCon/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "type": "TUTOR",
            "uuid": %s,
            "notifToken": "passTest",
            "permis": true
        }
                """.formatted(uuid)))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"role\":0}"));
    }

    /** RU: старая авторизованная подписка пользователя существует
     * новая заменяется старой */
    @Test @Tag("infCon")
    @CustomUser
    void infCon_whenGood_AdminUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        mainService.subscriptions.put(uuid, mock(Subscriber.class, Answers.RETURNS_DEEP_STUBS));
        mockMvc.perform(patch("/auth/infCon/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "type": "TUTOR",
            "uuid": %s,
            "notifToken": "passTest",
            "permis": true
        }
                """.formatted(uuid)))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"role\":0}"));
    }

    /** RU: завершение сеанса */
    @Test @Tag("remCon")
    @CustomUser
    void remCon_whenGood_AdminUser() throws Exception {
        SseEmitter sseEmitter = mock(SseEmitter.class);
        getSub().setSSE(sseEmitter);
        mockMvc.perform(patch("/auth/remCon/"))
            .andExpect(status().isOk());
    }

    /** RU: не успешная авторизация пользователя */
    @Test @Tag("auth")
    @CustomUser
    void auth_whenWrong_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByLogin("nm")).thenReturn(user);
        when(user.getPassword()).thenReturn("passTest1");
        mockMvc.perform(post("/auth/auth/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "notifToken": "uuidTest",
            "password": "passTest",
            "permis": true
        }
            """)).andExpect(status().isNotFound());
    }

    /** RU: авторизация пользователя */
    @Test @Tag("auth")
    @CustomUser
    void auth_whenGood_AdminUser() throws Exception {
        String uuid = ((CustomToken) SecurityContextHolder.getContext()
                .getAuthentication()).getUUID();
        User user = getSub().getUser();
        when(dbService.userByLogin("nm")).thenReturn(user);
        when(user.getPassword()).thenReturn("passTest");
        mockMvc.perform(post("/auth/auth/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "notifToken": "uuidTest",
            "password": "passTest",
            "permis": true
        }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"auth\":true,\"role\":0,\"uuidS\":\"" + uuid + "\",\"ico\":0,\"roles\":false,\"secFr\":false,\"email\":false}"));
    }

    /** RU: существующий логин */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenWrongLogin_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);
        mockMvc.perform(post("/auth/reg/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isNotFound());
    }

    /** RU: инвайт неверный */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenWrong_AdminUser() throws Exception {
        when(dbService.userByCode("uuidTest")).thenReturn(null);
        when(dbService.userByLogin("nm")).thenReturn(null);
        mockMvc.perform(post("/auth/reg/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isAccepted())
            .andExpect(content().string("{\"error\":\"noInv\"}"));
    }

    /** RU: регистирирует пользователя нового */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenGood_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);
        when(dbService.userByLogin("nm")).thenReturn(null);
        mockMvc.perform(post("/auth/reg/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm",
            "code": "uuidTest",
            "mod": "inv",
            "ico": "3",
            "secFr": "secretTest",
            "par": "passTest"
        }
            """)).andExpect(status().isCreated());
    }

    /** RU: админ
     * не успешно проверяет инвайт код для пользователя и отправляет 404 код клиенту */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenWrong_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode(null)).thenReturn(null);
        mockMvc.perform(post("/auth/checkInvCode/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * успешно проверяет инвайт код для пользователя */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenGood_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode("uuidTest")).thenReturn(user);
        mockMvc.perform(post("/auth/checkInvCode/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "code": "uuidTest"
        }
            """)).andExpect(status().isOk());
    }

    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenEmpty_AdminUser() throws Exception {
        mockMvc.perform(patch("/auth/setCodePep/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenGood_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(getSub().getUser().getRoles().containsKey(4L))
            .thenReturn(true);
        when(getSub().getUser().getSelRole()).thenReturn(4L);
        mockMvc.perform(patch("/auth/setCodePep/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": "nm12"
        }
            """)).andExpect(status().isOk());
    }
}