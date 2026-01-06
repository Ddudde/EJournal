package ru;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.google.gson.Gson;
import config.BeanConfig;
import config.SubscriberMethodArgumentResolver;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.configs.SecurityConfig;
import ru.security.ControllerExceptionHandler;
import utils.TestUtils;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static utils.TestUtils.defaultDescription;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@Import({BeanConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTestIntegration {
    protected static final TestUtils TEST_UTILS = new TestUtils();
    private static final String DESCRIPTION_DOCS = "UUID-токен, авторизация, в ней подписка и пользователь";
    protected MockMvc mockMvc;
    protected static final Gson gson = new Gson();
    private static final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();
    private static final SubscriberMethodArgumentResolver subscriberMethodArgumentResolver = new SubscriberMethodArgumentResolver();
    private static final SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
    private static final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
    protected Object testController;
    protected String nameTestedClass;

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

    /**
     * RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     *
     * @param summary    Заголовок эндпоинта
     * @param methodName Название теста
     * @return Сниппет
     */
    protected RestDocumentationResultHandler defaultSwaggerDocs(String summary, String methodName) {
        final ResourceSnippetParametersBuilder snip = ResourceSnippetParameters.builder()
            .summary(summary)
            .description(defaultDescription)
            .tag(nameTestedClass).requestFields()
            .requestHeaders(headerWithName(SecurityConfig.authTokenHeader)
                .description(DESCRIPTION_DOCS));
        return document(nameTestedClass + "/" + methodName, resource(snip.build()));
    }
}
