package ru.controllers.school.analytics;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getAuth;

public class PeriodControllerTest extends AbstractTestIntegration {
    private final IDBService dbService;
    private final ISSEService sseService;
    private static final String addPer_Summary = "[start] отправляет данные о расписании периодов обучения учебного центра";
    private static final String getInfo_Summary = "[start] отправляет данные о расписании периодов обучения учебного центра";

    @Captor
    private ArgumentCaptor<Object> answer;

    @Autowired
    public PeriodControllerTest(IDBService dbService, ISSEService sseService, PeriodController periodController) {
        this.dbService = dbService;
        this.sseService = sseService;
        this.testController = periodController;
        nameTestedClass = "PeriodsController";
    }

    @Test @Tag("addPer")
    @CustomAuth
    void addPer_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(post("/periods/addPer")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(addPer_Summary, "addPer_whenEmpty_Anonim"));
    }

    @Test @Tag("addPer")
    @CustomUser(roles = Roles.HTEACHER)
    void addPer_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getAuth().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getPeriods()).thenReturn(new ArrayList<>(TEST_UTILS.periods));
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(post("/periods/addPer")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "perN": "12.11.23",
                "perK": "29.12.23",
                "name": "II четверть"
            }
            """)).andExpect(status().isCreated())
            .andDo(defaultSwaggerDocs(addPer_Summary, "addPer_whenGood_HTEACHER"));

        verify(sseService).sendEventFor(any(), eq("addPerC"), answer.capture(), any(), any(), any(), any(), any());
        assertEquals("{\"body\":{\"name\":\"II четверть\",\"perN\":\"12.11.23\",\"perK\":\"29.12.23\"}}",
            gson.toJson(answer.getValue()));
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenEmpty_Anonim() throws Exception {
        when(dbService.userByLogin(any())).thenReturn(null);

        mockMvc.perform(get("/periods/getInfo")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenEmpty_Anonim"));
    }

    @Test @Tag("getInfo")
    @CustomUser(roles = Roles.HTEACHER)
    void getInfo_whenGood_HTEACHER() throws Exception {
        final User user = dbService.userById(getAuth().getUserId());
        final School sch1 = mock(School.class);
        when(sch1.getPeriods()).thenReturn(TEST_UTILS.periods);
        user.getSelecRole().setYO(sch1);

        mockMvc.perform(get("/periods/getInfo")
                .header(SecurityConfig.SSE_TOKEN_HEADER, AppConfig.TEST_SSE_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyP\":{\"0\":{\"name\":\"I четверть\",\"perN\":\"01.09.23\",\"perK\":\"03.11.23\"},\"1\":{\"name\":\"II четверть\",\"perN\":\"12.11.23\",\"perK\":\"29.12.23\"},\"2\":{\"name\":\"III четверть\",\"perN\":\"12.01.24\",\"perK\":\"29.03.24\"},\"3\":{\"name\":\"IV четверть\",\"perN\":\"01.04.24\",\"perK\":\"30.08.24\"}}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_HTEACHER"));
    }
}