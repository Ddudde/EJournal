package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.JsonObject;
import config.CustomAuth;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.SSE.Subscriber;
import ru.data.models.Syst;
import ru.data.models.school.School;
import ru.security.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;
import ru.services.db.IniDBService;
import utils.RandomUtils;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class TestControllerTest {

    private final DBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);

    private final MainService mainService = new MainService(null, dbService, null);

    @Spy
    private IniDBService iniDBService = new IniDBService(mainService);

    @Mock
    private AuthController authController;

    @InjectMocks
    private TestController testController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
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
            .tag("TestController").requestFields()
            .requestHeaders(headerWithName(SecurityConfig.authHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("TestController/" + methodName, resource(snip.build()));
    }

    private final String chTests_Summary = "Изменяет параметры тестирования";

    /** RU: аноним
     * отправляет 404 код-ответ */
    @Test @Tag("chTests")
    @CustomAuth
    void chTests_whenBad_Anonim() throws Exception {
        mockMvc.perform(patch("/test/chTests")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(chTests_Summary, "chTests_whenBad_Anonim"));
    }

    /** RU: админ
     * выключает режим тестирования и отправляет JSON'ом тестовую инфу */
    @Test @Tag("chTests")
    @CustomUser
    void chTests_whenGood_AdminUser() throws Exception {
        List<School> schools = new ArrayList<>(asList(
            randomUtils.getSchool(9600, "Лицей №3293", 2616, 4866, 4117),
            randomUtils.getSchool(1137, "Гимназия №2246", 3146, 4701, 5506),
            randomUtils.getSchool(2903, "Школа №217", 1973, 5375, 7100)
        ));
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(randomUtils.usersTest);
        iniDBService.getSchools().addAll(schools);
        when(dbService.getLessonRepository().uniqTeachersUBySchool(anyLong())).thenReturn(randomUtils.usersTest);

        mockMvc.perform(patch("/test/chTests")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
            "id": "checkbox_test",
            "val": false
        }
            """))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyT\":{\"admins\":{},\"schools\":{}}}"))
            .andDo(default_Docs(chTests_Summary, "chTests_whenGood_AdminUser"));
    }

    private final String getInfo_Summary = "[start] Отправка инфы для тестов";

    /** RU: аноним
     * отправляет 404 код-ответ */
    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenBad_AnonimUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isNotFound())
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenBad_AnonimUser"));
    }

    /** RU: админ, режим тестирования отключён
     * отправляет JSON'ом тестовую инфу */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOff_AdminUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true},\"bodyT\":{\"admins\":{},\"schools\":{}}}"))
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenGood_TestModeOff_AdminUser"));
    }

    /** RU: админ, включён режим тестирования
     * отправляет JSON'ом тестовую инфу */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOn_AdminUser() throws Exception {
        List<School> schools = new ArrayList<>(asList(
            randomUtils.getSchool(9600, "Лицей №3293", 2616, 4866, 4117),
            randomUtils.getSchool(1137, "Гимназия №2246", 3146, 4701, 5506),
            randomUtils.getSchool(2903, "Школа №217", 1973, 5375, 7100)
        ));
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(randomUtils.usersTest);
        iniDBService.getSchools().addAll(schools);
        when(dbService.getLessonRepository().uniqTeachersUBySchool(anyLong())).thenReturn(randomUtils.usersTest);

        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authHeader, "9693b2a1-77bb-4426-8045-9f9b4395d454"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true},\"bodyT\":{\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"2616\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4866\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4117\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"3146\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4701\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"5506\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"1973\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"5375\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"7100\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}}}}}"))
            .andDo(default_Docs(getInfo_Summary, "getInfo_whenGood_TestModeOn_AdminUser"));
    }
}