package ru.mirea.controllers.main;

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
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.security.CustomToken;
import ru.mirea.services.EmailService;
import ru.mirea.services.MainService;
import ru.mirea.services.PushService;
import ru.mirea.services.db.DBService;
import ru.mirea.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
public class SettingsControllerTest {

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

    @Mock
    private AuthController authController;

    @InjectMocks
    private SettingsController settingsController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
            .setMessageConverters(converter)
            .addFilters(authInjector).build();
    }

    private Subscriber getSub(){
        return ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication()).getSub();
    }

    /** RU: админ
     * клиент не отправляет данные и получает 404 код */
    @Test @Tag("checkCodeEmail")
    @CustomUser
    void checkCodeEmail_whenEmpty_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(user.getSettings().getEmailCode()).thenReturn("code");
        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * сверяет код подтверждения и меняет емейл */
    @Test @Tag("checkCodeEmail")
    @CustomUser
    void checkCodeEmail_whenGood_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode("uuid")).thenReturn(user);
        when(user.getSettings().getEmailCode()).thenReturn("code");
        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "invCod": "uuid",
            "emailCode": "code",
            "email": "test@mail.com"
        }
            """)).andExpect(status().isOk());
    }

    /** RU: админ
     * клиент не отправляет данных и получает 404 код */
    @Test @Tag("startEmail")
    @CustomUser
    void startEmail_whenEmpty_AdminUser() throws Exception {
        mockMvc.perform(patch("/settings/startEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * отправляет код на почту при регистрации */
    @Test @Tag("startEmail")
    @CustomUser
    void startEmail_whenGood_AdminUser() throws Exception {
        User user = getSub().getUser();
        when(dbService.userByCode("uuid")).thenReturn(user);
        mockMvc.perform(patch("/settings/startEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "invCod": "uuid",
            "email": "test@mail.com"
        }
            """)).andExpect(status().isOk());
        verify(emailService, times(1)).sendRegCode(eq("test@mail.com"), any());
    }

    /** RU: админ
     * клиент не отправляет токен и получает 404 код */
    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenEmpty_AdminUser() throws Exception {
        mockMvc.perform(post("/settings/remNotifToken/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * удаляет токен клиента */
    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenGood_AdminUser() throws Exception {
        mockMvc.perform(post("/settings/remNotifToken/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "notifToken": "testtoken"
        }
            """)).andExpect(status().isOk());
        verify(pushService, times(1)).remToken(any(), eq("testtoken"));
    }

    /** RU: админ
     * клиент не отправляет токен и получает 404 код */
    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenEmpty_AdminUser() throws Exception {
        mockMvc.perform(post("/settings/addNotifToken/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * устанавливает токен для уведомлений клиента */
    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenGood_AdminUser() throws Exception {
        mockMvc.perform(post("/settings/addNotifToken/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "notifToken": "testtoken"
        }
            """)).andExpect(status().isOk());
        verify(pushService, times(1)).addToken(any(), eq("testtoken"));
    }

    /** RU: админ
     * клиент не отправляет данных и получает 404-ый код */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenEmpty_AdminUser() throws Exception {
        mockMvc.perform(patch("/settings/chSettings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * включает уведомления о новых заявках на подключение школ */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenGood_AdminUser() throws Exception {
        SettingUser settingUser = mock(SettingUser.class);
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/chSettings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": "checkbox_notify_new_sch",
            "val": true
        }
            """)).andExpect(status().isOk());
        verify(settingUser, times(1)).setNNewReqSch(eq(true));
    }

    /** RU: админ
     * не подтверждает код с эмейла и отправляет 404 ответ */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenWrongCode_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("11112");
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "emailCode": "1111",
            "nPar": "1234"
        }
            """)).andExpect(status().isNotFound());
        verify(user, times(0)).setPassword(eq("1234"));
    }

    /** RU: админ
     * подтверждает код с эмейла и меняет пароль пользователя */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenGood_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("1111");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "emailCode": "1111",
            "nPar": "1234"
        }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{}"));
        verify(user, times(1)).setPassword(eq("1234"));
    }

    /** RU: админ
     * неуспешно сверяет секретную фразу и отправляет клиенту причину */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenError_secFr_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret1");
        User user = getSub().getUser();
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/chPass/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "emailSt": false,
            "secFR": "victoria_secret"
        }
            """)).andExpect(status().isAccepted())
            .andExpect(content().string("{\"error\":\"secFr\"}"));
    }

    /** RU: админ
     * сверяет секретную фразу и меняет пароль  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_secFr_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/chPass/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "emailSt": false,
            "secFR": "victoria_secret",
            "nPar": "passs"
        }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{}"));
        verify(user, times(1)).setPassword(eq("passs"));
    }

    /** RU: админ
     * отправляет код подтверждения на почту  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_Email_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setEmail("test@mail.com");
        User user = getSub().getUser();
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);
        mockMvc.perform(patch("/settings/chPass/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "login": "nm12",
            "emailSt": true,
            "email": "test@mail.com"
        }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{}"));
        verify(emailService, times(1)).sendRecCode(eq("test@mail.com"), any(), any());
    }

    /** RU: админ
     * клиент не отправляет данные и получает 404 ответ */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenEmpty_AdminUser() throws Exception {
        when(getSub().getUser().getSettings()).thenReturn(null);
        mockMvc.perform(get("/settings/getSettings/"))
            .andExpect(status().isNotFound());
    }

    /** RU: админ
     * отправляет JSON'ом настройки пользователя клиенту */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenGood_AdminUser() throws Exception {
        SettingUser settingUser = new SettingUser();
        settingUser.setNNewReqSch(true);
        when(getSub().getUser().getSettings()).thenReturn(settingUser);
        mockMvc.perform(get("/settings/getSettings/"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"checkbox_hints\":true,\"checkbox_notify\":false,\"checkbox_notify_sched\":false,\"checkbox_notify_marks\":false,\"checkbox_notify_yo\":false,\"checkbox_notify_por\":false,\"checkbox_notify_new_sch\":true}"));
    }
}