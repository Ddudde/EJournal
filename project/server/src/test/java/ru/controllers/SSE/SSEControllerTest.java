package ru.controllers.SSE;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import config.CustomUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.AbstractTestIntegration;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.CustomToken;
import ru.services.MainService;

import java.util.UUID;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.defaultDescription;

public class SSEControllerTest extends AbstractTestIntegration {
    private MockedStatic staticMockMainService;

    @Autowired
    public SSEControllerTest(SSEController sseController) {
        this.testController = sseController;
        nameTestedClass = "SSEController";
    }

    @AfterEach
    void afterEach() {
        staticMockMainService.close();
        staticMockSSE.close();
    }

    @BeforeEach
    void setUp() {
        staticMockMainService = Mockito.mockStatic(MainService.class);
    }

    /** RU: записывает ответ и тело запроса от теста эндпонта в Swagger вместе с описанием эндпоинта и именем теста
     * @param methodName Название теста
     * @return Сниппет */
    private RestDocumentationResultHandler swaggerDocs(String methodName) {
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
            .andDo(swaggerDocs("start_whenGoodNext_Anonim"));
        assertNotEquals(0, MainService.subscriptions.size());
    }

    /** RU: стартует со старой подпиской */
    @Test @Tag("start")
    @CustomUser
    void start_whenGood_AdminUser() throws Exception {
        final CustomToken cu = ((CustomToken) SecurityContextHolder.getContext()
            .getAuthentication());
        final String uuid = cu.getUUID();
        final SubscriberDTO sub = mock(SubscriberDTO.class, Answers.RETURNS_DEEP_STUBS);
        when(sub.getLogin()).thenReturn("nm12");
        MainService.subscriptions.put(UUID.fromString(uuid), sub);

        mockMvc.perform(get("/sse/start/{uuidAuth}", uuid))
            .andExpect(status().isOk())
            .andDo(swaggerDocs("start_whenGood_AdminUser"));
        staticMockMainService.verify(() -> MainService.setSSE(eq(sub), any(), eq(UUID.fromString(uuid))));
    }
}