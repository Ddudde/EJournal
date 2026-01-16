package ru.controllers.main;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.services.IEmailService;
import ru.services.IPushService;
import ru.services.db.IDBService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getSub;

public class SettingsControllerTest extends AbstractTestIntegration {
    private final IDBService dbService;
    private final IEmailService emailService;
    private final IPushService pushService;
    private final PasswordEncoder passwordEncoder;
    private static final String checkCodeEmail_Summary = "Подтверждение емэйла";
    private static final String startEmail_Summary = "Изменение электронной почты пользователя или добавление при регистрации";
    private static final String remNotifToken_Summary = "Удаление токена уведомлений";
    private static final String addNotifToken_Summary = "Установка токена уведомлений";
    private static final String chSettings_Summary = "Вкл/выкл подсказки или ряд уведомлений";
    private static final String checkPasCodeEmail_Summary = "Изменяет пароль пользователя при помощи емэйла";
    private static final String chPass_Summary = "Изменяет пароль пользователя при помощи емэйла/секретной фразы";
    private static final String getSettings_Summary = "Отправляет настройки клиенту";

    @Autowired
    public SettingsControllerTest(IDBService dbService, IEmailService emailService, IPushService pushService, PasswordEncoder passwordEncoder, SettingsController settingsController) {
        this.dbService = dbService;
        this.emailService = emailService;
        this.pushService = pushService;
        this.passwordEncoder = passwordEncoder;
        this.testController = settingsController;
        nameTestedClass = "SettingsController";
    }

    @Test @Tag("checkCodeEmail")
    @CustomAuth
    void checkCodeEmail_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        
        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkCodeEmail_Summary, "checkCodeEmail_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * сверяет код подтверждения и меняет емейл */
    @Test @Tag("checkCodeEmail")
    @CustomUser
    void checkCodeEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userByCode("uuid")).thenReturn(user);
        user.getSettings().setEmailCode("code");

        mockMvc.perform(patch("/settings/checkCodeEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "invCod": "uuid",
                "emailCode": "code",
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkCodeEmail_Summary, "checkCodeEmail_whenGood_AdminUser"));
    }

    @Test @Tag("startEmail")
    @CustomAuth
    void startEmail_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        
        mockMvc.perform(patch("/settings/startEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(startEmail_Summary, "startEmail_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * отправляет код на почту при регистрации */
    @Test @Tag("startEmail")
    @CustomUser
    void startEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userByCode("uuid")).thenReturn(user);

        mockMvc.perform(patch("/settings/startEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "invCod": "uuid",
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(startEmail_Summary, "startEmail_whenGood_AdminUser"));

        verify(emailService, times(1)).sendRegCode(eq("test@mail.com"), any());
    }

    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(post("/settings/remNotifToken/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remNotifToken_Summary, "remNotifToken_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * удаляет токен клиента */
    @Test @Tag("remNotifToken")
    @CustomUser
    void remNotifToken_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        
        mockMvc.perform(post("/settings/remNotifToken/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "testtoken"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(remNotifToken_Summary, "remNotifToken_whenGood_AdminUser"));

        verify(pushService, times(1)).remToken(any(), eq("testtoken"));
    }

    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(post("/settings/addNotifToken/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))

            .andExpect(statusCode)

            .andDo(defaultSwaggerDocs(addNotifToken_Summary, "addNotifToken_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * устанавливает токен для уведомлений клиента */
    @Test @Tag("addNotifToken")
    @CustomUser
    void addNotifToken_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        
        mockMvc.perform(post("/settings/addNotifToken/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "testtoken"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(addNotifToken_Summary, "addNotifToken_whenGood_AdminUser"));

        verify(pushService, times(1)).addToken(any(), eq("testtoken"));
    }

    /** RU: админ
     * клиент не отправляет данных и получает 404-ый код */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        
        mockMvc.perform(patch("/settings/chSettings/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSettings_Summary, "chSettings_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * включает уведомления о новых заявках на подключение школ */
    @Test @Tag("chSettings")
    @CustomUser
    void chSettings_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = mock(SettingUser.class);
        User user = dbService.userById(getSub().getUserId());
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chSettings/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "checkbox_notify_new_sch",
                "val": true
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chSettings_Summary, "chSettings_whenGood_AdminUser"));
        verify(settingUser, times(1)).setNNewReqSch(eq(true));
    }

    /** RU: админ
     * не подтверждает код с эмейла и отправляет 404 ответ */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenWrongCode_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("11112");
        User user = dbService.userById(getSub().getUserId());
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "emailCode": "1111",
                "nPar": "1234"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkPasCodeEmail_Summary, "checkPasCodeEmail_whenWrongCode_AdminUser"));

        verify(user, times(0)).setPassword(eq("1234"));
    }

    /** RU: админ
     * подтверждает код с эмейла и меняет пароль пользователя */
    @Test @Tag("checkPasCodeEmail")
    @CustomUser
    void checkPasCodeEmail_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmailCode("1111");
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/checkPasCodeEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailCode": "1111",
                "nPar": "1234"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(checkPasCodeEmail_Summary, "checkPasCodeEmail_whenGood_AdminUser"));

        verify(passwordEncoder, times(1)).encode(eq("1234"));
    }

    /** RU: админ
     * неуспешно сверяет секретную фразу и отправляет клиенту причину */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenError_secFr_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isAccepted();
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret1");
        User user = dbService.userById(getSub().getUserId());
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "emailSt": false,
                "secFR": "victoria_secret"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{\"error\":\"secFr\"}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenError_secFr_AdminUser"));
    }

    /** RU: админ
     * сверяет секретную фразу и меняет пароль  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_secFr_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setSecFr("victoria_secret");
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailSt": false,
                "secFR": "victoria_secret",
                "nPar": "passs"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenGood_secFr_AdminUser"));

        verify(passwordEncoder, times(1)).encode(eq("passs"));
    }

    /** RU: админ
     * отправляет код подтверждения на почту  */
    @Test @Tag("chPass")
    @CustomUser
    void chPass_whenGood_Email_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setEmail("test@mail.com");
        User user = dbService.userById(getSub().getUserId());
        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(user.getSettings()).thenReturn(settingUser);

        mockMvc.perform(patch("/settings/chPass/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "emailSt": true,
                "email": "test@mail.com"
            }
            """)).andExpect(statusCode)
            .andExpect(content().string("{}"))
            .andDo(defaultSwaggerDocs(chPass_Summary, "chPass_whenGood_Email_AdminUser"));
        verify(emailService, times(1)).sendRecCode(eq("test@mail.com"), any(), any());
    }

    /** RU: админ
     * клиент не отправляет данные и получает 404 ответ */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenEmpty_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isNotFound();
        when(dbService.userById(getSub().getUserId()).getSettings()).thenReturn(null);

        mockMvc.perform(get("/settings/getSettings/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(getSettings_Summary, "getSettings_whenEmpty_AdminUser"));
    }

    /** RU: админ
     * отправляет JSON'ом настройки пользователя клиенту */
    @Test @Tag("getSettings")
    @CustomUser
    void getSettings_whenGood_AdminUser() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        SettingUser settingUser = new SettingUser();
        settingUser.setNNewReqSch(true);
        when(dbService.userById(getSub().getUserId()).getSettings()).thenReturn(settingUser);

        mockMvc.perform(get("/settings/getSettings/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"checkbox_hints\":true,\"checkbox_notify\":false,\"checkbox_notify_sched\":false,\"checkbox_notify_marks\":false,\"checkbox_notify_yo\":false,\"checkbox_notify_por\":false,\"checkbox_notify_new_sch\":true}"))
            .andDo(defaultSwaggerDocs(getSettings_Summary, "getSettings_whenGood_AdminUser"));
    }
}