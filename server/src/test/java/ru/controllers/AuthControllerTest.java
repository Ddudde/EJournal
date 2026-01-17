package ru.controllers;

import config.CustomUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getSub;

@Slf4j
public class AuthControllerTest extends AbstractTestIntegration {
    private final IDBService dbService;
    private final ISSEService sseService;
    private static final String infCon_Summary = "[start] Изменение подписки";
    private static final String remCon_Summary = "Завершение сеанса";
    private static final String auth_Summary = "Авторизация пользователя";
    private static final String reg_Summary = "Регистрация пользователя";
    private static final String checkInvCode_Summary = "Проверка инвайта для регистрации/регистрации новой роли";
    private static final String setCodePep_Summary = "Установка/обновление инвайта для регистрации + Server Sent Events";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Captor
    private ArgumentCaptor<Object> obj;

    @Autowired
    AuthControllerTest(IDBService dbService, ISSEService sseService, AuthController authController) {
        this.dbService = dbService;
        this.sseService = sseService;
        this.testController = authController;
        nameTestedClass = "AuthController";
    }

    private CustomToken getAuth() {
        return (CustomToken) SecurityContextHolder.getContext().getAuthentication();
    }

    /** RU: старая авторизованная подписка пользователя существует
     * новая заменяется старой */
    @Test @Tag("infCon")
    @CustomUser
    void infCon_whenGood_AdminUser() throws Exception {
        mockMvc.perform(patch("/auth/infCon/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "login": "nm12",
                "type": "TUTOR",
                "notifToken": "passTest",
                "permis": true
            }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"role\":4}"))
            .andDo(defaultSwaggerDocs(infCon_Summary, "infCon_whenGood_AdminUser"));
    }

    /** RU: админ
     * завершение сеанса */
    @Test @Tag("remCon")
    @CustomUser
    void remCon_whenGood_AdminUser() throws Exception {
        final SseEmitter sseEmitter = mock(SseEmitter.class);
        getSub().setSSE(sseEmitter);

        mockMvc.perform(patch("/auth/remCon/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(remCon_Summary, "remCon_whenGood_AdminUser"));
    }

    /** RU: аноним
     * не успешная авторизация пользователя, муляжный тест, показывает общую логику работы API
     * toDo: перенести в тест Security */
    @Test @Tag("auth")
    @CustomUser
    void auth_whenWrong_AdminUser() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        final CustomToken newAuth = new CustomToken(new SubscriberDTO(), UUID.randomUUID().toString());
//        when(dbService.userByLogin("nm12")).thenReturn(user);
        when(dbService.existUserBySubscription(any())).thenReturn(false);
        when(user.getPassword()).thenReturn("passTest1");
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        log.trace(getAuth() + "");

        mockMvc.perform(post("/auth/auth/")
                .header(HttpHeaders.AUTHORIZATION, "Basic bm0xMjpwYXNzVGVzdA==")// Basic Auth
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "uuidTest",
                "permis": true
            }
            """)).andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(auth_Summary, "auth_whenWrong_AdminUser"));
    }

    /** RU: админ
     * авторизация пользователя, муляжный тест, показывает общую логику работы API
     * toDo: перенести в тест Security */
    @Test @Tag("auth")
    @CustomUser(password = "passTest")
    void auth_whenGood_AdminUser() throws Exception {
        final String uuid = getAuth().getUUID();
//        SecurityContextHolder.getContext().setAuthentication(new CustomToken());

        mockMvc.perform(post("/auth/auth/")
                .header(HttpHeaders.AUTHORIZATION, "Basic bm0xMjpwYXNzVGVzdA==")// Basic Auth
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "uuidTest",
                "permis": true
            }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"role\":4,\"auth\":true,\"login\":\"nm12\",\"uuidS\":\"" + uuid + "\",\"roles\":true,\"secFr\":false,\"email\":false}"))
            .andDo(defaultSwaggerDocs(auth_Summary, "auth_whenGood_AdminUser"));
    }

    /** RU: существующий логин */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenWrongLogin_Anonim() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        when(dbService.userByCode("uuidTest")).thenReturn(user);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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
            """)).andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(reg_Summary, "reg_whenWrongLogin_Anonim"));
    }

    /** RU: инвайт неверный */
    @Test @Tag("reg")
    void reg_whenWrong_Anonim() throws Exception {
        when(dbService.userByCode("uuidTest")).thenReturn(null);
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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
            .andExpect(content().string("{\"error\":\"noInv\"}"))
            .andDo(defaultSwaggerDocs(reg_Summary, "reg_whenWrong_Anonim"));
    }

    /** RU: регистирирует пользователя нового */
    @Test @Tag("reg")
    @CustomUser
    void reg_whenGood_Anonim() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        when(dbService.userByCode("uuidTest")).thenReturn(user);
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(post("/auth/reg/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
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
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(reg_Summary, "reg_whenGood_Anonim"));
    }

    /** RU: админ
     * не успешно проверяет инвайт код для пользователя и отправляет 404 код клиенту */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenWrong_AdminUser() throws Exception {
        when(dbService.userByCode(null)).thenReturn(null);

        mockMvc.perform(post("/auth/checkInvCode/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(checkInvCode_Summary, "checkInvCode_whenWrong_AdminUser"));
    }

    /** RU: админ
     * успешно проверяет инвайт код для пользователя */
    @Test @Tag("checkInvCode")
    @CustomUser
    void checkInvCode_whenGood_AdminUser() throws Exception {
        final User user = dbService.userById(getSub().getUserId());
        when(dbService.userByCode("uuidTest")).thenReturn(user);

        mockMvc.perform(post("/auth/checkInvCode/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "code": "uuidTest"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(checkInvCode_Summary, "checkInvCode_whenGood_AdminUser"));
    }
    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenEmpty_AdminUser() throws Exception {
        when(dbService.userByLogin(null)).thenReturn(null);

        mockMvc.perform(patch("/auth/setCodePep/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(defaultSwaggerDocs(setCodePep_Summary, "setCodePep_whenEmpty_AdminUser"));
        verify(sseService, times(0)).sendEventFor(any(), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * устанавливает инвайт код для пользователя */
    @Test @Tag("setCodePep")
    @CustomUser
    void setCodePep_whenGood_AdminUser() throws Exception {
        final User user = dbService.userById(getSub().getUserId());

        mockMvc.perform(patch("/auth/setCodePep/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "nm12"
            }
            """)).andExpect(status().isOk())
            .andDo(defaultSwaggerDocs(setCodePep_Summary, "setCodePep_whenGood_AdminUser"));
        verify(user).setCode((String) obj.capture());
        verify(sseService, times(2)).sendEventFor(any(), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"id\":9764,\"id1\":0,\"code\":\"%s\"}".formatted(obj.getValue().toString()),
            gson.toJson(answer.getValue()));
    }
}