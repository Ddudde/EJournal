package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import config.CustomAuth;
import config.CustomUser;
import config.SubscriberMethodArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.data.models.Syst;
import ru.data.models.school.School;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
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
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.RandomUtils.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({TestControllerConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final RandomUtils randomUtils = new RandomUtils();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    private final String bearerToken = "9693b2a1-77bb-4426-8045-9f9b4395d454";

    @Autowired
    private DBService dbService;

    @Autowired
    private IniDBService iniDBService;

    @Autowired
    private TestController testController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .setCustomArgumentResolvers(subscriberMethodArgumentResolver)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    /** RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     * @param summary Заголовок эндпоинта
     * @param methodName Название теста
     * @return Сниппет */
    private RestDocumentationResultHandler defaultSwaggerDocs(String summary, String methodName) {
        ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag("TestController").requestFields()
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description("UUID-токен, авторизация, в ней подписка и пользователь"));
        return document("TestController/" + methodName, resource(snip.build()));
    }

    private final String chTests_Summary = "Изменяет параметры тестирования";

    @Test @Tag("chTests")
    @CustomAuth
    void chTests_whenBad_Anonim() throws Exception {
        mockMvc.perform(put("/test/chTests")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chTests_Summary, "chTests_whenBad_Anonim"));
    }

    /** RU: админ
     * выключает режим тестирования и отправляет JSON'ом тестовую инфу */
    @Test @Tag("chTests")
    @CustomUser
    void chTests_whenGood_AdminUser() throws Exception {
        List<School> schools = new ArrayList<>(asList(
            getSchool(9600, "Лицей №3293", 2616, 4866, 4117),
            getSchool(1137, "Гимназия №2246", 3146, 4701, 5506),
            getSchool(2903, "Школа №217", 1973, 5375, 7100)
        ));
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(usersTest);
        iniDBService.getSchools().addAll(schools);
        when(dbService.getLessonRepository().uniqTeachersUBySchool(anyLong())).thenReturn(usersTest);

        mockMvc.perform(put("/test/chTests")
                .header(SecurityConfig.authTokenHeader, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "checkbox_test",
                "val": false
            }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"bodyT\":{\"admins\":{},\"schools\":{}}}"))
            .andDo(defaultSwaggerDocs(chTests_Summary, "chTests_whenGood_AdminUser"));
    }

    private final String getInfo_Summary = "[start] Отправка инфы для тестов";

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenBad_AnonimUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenBad_AnonimUser"));
    }

    /** RU: админ, режим тестирования отключён
     * отправляет JSON'ом тестовую инфу */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOff_AdminUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true},\"bodyT\":{\"admins\":{},\"schools\":{}}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_TestModeOff_AdminUser"));
    }

    /** RU: админ, включён режим тестирования
     * отправляет JSON'ом тестовую инфу */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOn_AdminUser() throws Exception {
        prepareSchools();

        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, bearerToken))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true},\"bodyT\":{\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\"}}}}}}}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_TestModeOn_AdminUser"));
    }

    /** RU: создаём случайные учебные центры */
    private void prepareSchools() {
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(usersTest);
        iniDBService.getSchools().addAll(schools);
        when(dbService.getLessonRepository().uniqTeachersUBySchool(anyLong())).thenReturn(usersTest);
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class TestControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public IniDBService iniDBService(MainService mainService) {
        return spy(new IniDBService(mainService));
    }

    @Bean
    public AuthController authController() {
        return mock(AuthController.class);
    }

    @Bean
    public TestController testController(AuthController authController, IniDBService iniDBService) {
        return spy(new TestController(authController, iniDBService));
    }
}