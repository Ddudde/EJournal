package ru.controllers.main;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import config.CustomAuth;
import config.CustomUser;
import lombok.extern.slf4j.Slf4j;
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
import ru.data.DTO.service.data.UserServiceDTO;
import ru.security.user.Roles;
import ru.services.interfaces.IPushService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import utils.TestUtils;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;
import static utils.TestUtils.getSub;

@Slf4j
public class ProfileControllerTest extends AbstractTestIntegration {
    private final IDBService dbService;
    private final IPushService pushService;
    private final ISSEService sseService;
    private static final String chKid_Summary = "Изменение контроллируемого ученика у родителя";
    private static final String chRole_Summary = "Изменение роли на следующую по иерархии из имеющихся у пользователя";
    private static final String exit_Summary = "Выход с аккаунта";
    private static final String chEmail_Summary = "Изменение/добавление электронной почты определённой роли пользователя + Server Sent Events";
    private static final String chInfo_Summary = "Изменение/добавление дополнительной информации о пользователе + Server Sent Events";
    private static final String chLogin_Summary = "Изменение логина пользователя + Server Sent Events";

    @Captor
    private ArgumentCaptor<UserServiceDTO> answer;

    @Autowired
    public ProfileControllerTest(IDBService dbService, IPushService pushService, ISSEService sseService, ProfileController profileController) {
        this.dbService = dbService;
        this.pushService = pushService;
        this.sseService = sseService;
        this.testController = profileController;
        nameTestedClass = "ProfileController";
    }

    @Test @Tag("chKid")
    @CustomAuth
    void chKid_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chKid/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chKid_Summary, "chKid_whenEmpty_Anonim"));
    }

    @Test @Tag("chKid")
    @CustomUser(roles = Roles.PARENT)
    void chKid_whenGood_Parent() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chKid/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "idL": "123"
            }
            """)).andExpect(statusCode)
            .andExpect(content().json("{\"kid\":123}"))
            .andDo(defaultSwaggerDocs(chKid_Summary, "chKid_whenGood_Parent"));
    }

    @Test @Tag("chRole")
    @CustomAuth
    void chRole_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chRole/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chRole_Summary, "chRole_whenEmpty_Anonim"));
    }

    /** RU: ученик-администратор, с активной ролью ученика
     * отправляет 200 код-ответ и меняет роль на администратора */
    @Test @Tag("chRole")
    @CustomUser
    void chRole_whenGood_Kid() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        dbService.userById(getSub().getUserId()).setSelRole(Roles.KID);

        mockMvc.perform(patch("/profiles/chRole/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"role\":4}"))
            .andDo(defaultSwaggerDocs(chRole_Summary, "chRole_whenGood_Kid"));
    }

    @Test @Tag("exit")
    @CustomAuth
    void exit_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/exit/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(exit_Summary, "exit_whenEmpty_Anonim"));
    }

    @Test @Tag("exit")
    @CustomUser
    void exit_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/exit/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "notifToken": "notifTest"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(exit_Summary, "exit_whenGood_Admin"));
        verify(pushService).remToken(any(), eq("notifTest"));
    }

    @Test @Tag("chEmail")
    @CustomAuth
    void chEmail_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chEmail_Summary, "chEmail_whenEmpty_Anonim"));
        verify(sseService, times(0)).sendEventFor(eq("chEmail"), answer.capture(), any(), any(), any(), any(), any());
    }

    @Test @Tag("chEmail")
    @CustomUser
    void chEmail_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chEmail/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "email": "mail1@example.com"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chEmail_Summary, "chEmail_whenGood_Admin"));
        verify(sseService).sendEventFor(eq("chEmail"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"email\":\"mail1@example.com\",\"role\":4}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chInfo")
    @CustomAuth
    void chInfo_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(patch("/profiles/chInfo/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chInfo_Summary, "chInfo_whenEmpty_Anonim"));
        verify(sseService, times(0)).sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any());
    }

    @Test @Tag("chInfo")
    @CustomUser
    void chInfo_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(patch("/profiles/chInfo/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "info": "testInfo"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chInfo_Summary, "chInfo_whenGood_Admin"));
        verify(sseService).sendEventFor(eq("chInfo"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"more\":\"testInfo\"}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("chLogin")
    @CustomAuth
    void chLogin_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();

        mockMvc.perform(patch("/profiles/chLogin/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chLogin_Summary, "chLogin_whenEmpty_Anonim"));
        verify(sseService, times(0)).sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any());
    }

    /** RU: админ
     * отправляет JSON'ом информацию о старом и новом логине */
    @Test @Tag("chLogin")
    @CustomUser
    void chLogin_whenGood_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        getSub().setLvlMore2("nm12");
        when(dbService.userByLogin("nm")).thenReturn(null);

        mockMvc.perform(patch("/profiles/chLogin/")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "nLogin": "nm"
            }
            """)).andExpect(statusCode)
            .andDo(defaultSwaggerDocs(chLogin_Summary, "chLogin_whenGood_Admin"));
        verify(sseService).sendEventFor(eq("chLogin"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"oLogin\":\"nm12\",\"nLogin\":\"nm\"}}",
            gson.toJson(answer.getValue()));
    }

    private RestDocumentationResultHandler getProfile_Docs(String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Отправляет инфу профиля либо другого пользователя либо личную")
            .description(defaultDescription)
            .pathParameters(parameterWithName("login").optional()
                .type(SimpleType.STRING)
                .description("Логин пользователя")
            )
            .tag("ProfileController")
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("ProfileController/" + methodName, resource(snip.build()));
    }

    /** RU: аноним пытается посмотреть другого
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomAuth
    void getProfile_whenEmpty_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isUnauthorized();
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/profiles/getProfile/{login}/", "nm12")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andDo(getProfile_Docs("getProfile_whenEmpty_Anonim"));
    }

    /** RU: админ смотрит себя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomUser
    void getProfile_whenGood_AuthLogin_Admin() throws Exception {
        final ResultMatcher statusCode = status().isOk();

        mockMvc.perform(get("/profiles/getProfile/{login}/", "nm12")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"login\":\"nm12\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_AuthLogin_Admin"));
    }

    /** RU: аноним смотрит пользователя
     * отправляет JSON'ом информацию о пользователе */
    @Test @Tag("getProfile")
    @CustomAuth
    void getProfile_whenGood_CustomLogin_Anonim() throws Exception {
        final ResultMatcher statusCode = status().isOk();
        when(dbService.userByLogin("nm12")).thenReturn(TestUtils.usersTest.get(4));

        mockMvc.perform(get("/profiles/getProfile/{login}/", "nm12")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(statusCode)
            .andExpect(content().json("{\"login\":\"facere_a\",\"id\":9764,\"fio\":\"Силин А.К.\",\"roles\":{\"0\":{\"email\":\"example@mail.com\",\"parents\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}"))
            .andDo(getProfile_Docs("getProfile_whenGood_CustomLogin_Anonim"));
    }
}