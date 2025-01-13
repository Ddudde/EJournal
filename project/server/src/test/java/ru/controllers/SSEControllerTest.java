package ru.controllers;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
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
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.data.SSE.Subscriber;
import ru.security.ControllerExceptionHandler;
import ru.security.CustomAccessDenied;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;

import javax.servlet.ServletException;
import java.util.UUID;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import({SSEControllerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SSEControllerTest {
    private MockMvc mockMvc;
    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @Autowired
    private MainService mainService;

    @Autowired
    private SSEController sseController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws ServletException {
        authInjector.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(sseController)
            .setMessageConverters(converter)
            .setControllerAdvice(controllerExceptionHandler)
            .setCustomArgumentResolvers(subscriberMethodArgumentResolver)
            .apply(documentationConfiguration(restDocumentation))
            .addFilters(authInjector).build();
    }

    /** RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     * @param methodName Название теста
     * @return Сниппет */
    private RestDocumentationResultHandler defaultSwaggerDocs(String methodName) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary("[start] Открытие Server Sent Events для нового клиента или сохранение подписки для старого пользователя")
            .description(defaultDescription + """
        Подписка сохраняется в течении одного запуска сервера.
        """)
            .pathParameters(parameterWithName("uuidAuth").optional()
                .type(SimpleType.STRING)
                .description("Авторизация, в ней подписка и пользователь")
            )
            .tag("SSEController");
        return document("SSEController/" + methodName, resource(snip.build()));
    }

    /** RU: стартует для нового клиента */
    @Test @Tag("start")
    void start_whenGoodNext_Anonim() throws Exception {
        mockMvc.perform(get("/sse/start"))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs("start_whenGoodNext_Anonim"));
        assertNotEquals(0, mainService.subscriptions.size());
    }

    /** RU: стартует со старой подпиской */
    @Test @Tag("start")
    @CustomUser
    void start_whenGood_AdminUser() throws Exception {
        final CustomToken cu = ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication());
        final String uuid = cu.getUUID();
        final Subscriber sub = mock(Subscriber.class, Answers.RETURNS_DEEP_STUBS);
        when(sub.getLogin()).thenReturn("nm12");
        mainService.subscriptions.put(UUID.fromString(uuid), sub);

        mockMvc.perform(get("/sse/start/{uuidAuth}", uuid))
            .andExpect(status().isOk())
            .andDo(defaultSwaggerDocs("start_whenGood_AdminUser"));
        verify(sub).setSSE(any(), eq(UUID.fromString(uuid)));
    }
}

@TestConfiguration
@Import({CustomAccessDenied.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SSEControllerConfig {

    @Bean
    public DBService dbService() {
        return mock(DBService.class, Answers.RETURNS_DEEP_STUBS);
    }

    @Bean(initMethod = "postConstruct")
    public MainService mainService(DBService dbService) {
        return new MainService(null, dbService, null);
    }

    @Bean
    public SSEController sseController() {
        return spy(new SSEController());
    }
}