package ru.controllers;

import config.CustomAuth;
import config.CustomUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.AbstractTestIntegration;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.data.DAO.Syst;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.services.db.InitDBService;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.schools;
import static utils.TestUtils.usersTest;

public class TestControllerTest extends AbstractTestIntegration {
    private final LessonRepository lessonRepository;
    private final SchoolRepository schoolRepository;
    private final InitDBService initDBService;
    private static final String chTests_Summary = "Изменяет параметры тестирования";
    private static final String getInfo_Summary = "[start] Отправка инфы для тестов";

    @Autowired
    TestControllerTest(LessonRepository lessonRepository, SchoolRepository schoolRepository, InitDBService initDBService, TestController testController) {
        this.lessonRepository = lessonRepository;
        this.schoolRepository = schoolRepository;
        this.initDBService = initDBService;
        this.testController = testController;
        nameTestedClass = "TestController";
    }

    @Test @Tag("chTests")
    @CustomAuth
    void chTests_whenBad_Anonim() throws Exception {
        mockMvc.perform(put("/test/chTests")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(chTests_Summary, "chTests_whenBad_Anonim"));
    }

    /**
     * RU: админ
     * выключает режим тестирования и отправляет JSON'ом тестовую инфу
     */
    @Test @Tag("chTests")
    @CustomUser
    void chTests_whenGood_AdminUser() throws Exception {
        prepareSchools();

        mockMvc.perform(put("/test/chTests")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "id": "checkbox_test",
                "val": false
            }
            """)).andExpect(status().isOk())
            .andExpect(content().string("{\"bodyT\":{}}"))
            .andDo(defaultSwaggerDocs(chTests_Summary, "chTests_whenGood_AdminUser"));
    }

    @Test @Tag("getInfo")
    @CustomAuth
    void getInfo_whenBad_AnonimUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isUnauthorized())
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenBad_AnonimUser"));
    }

    /**
     * RU: админ, режим тестирования отключён
     * отправляет JSON'ом тестовую инфу
     */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOff_AdminUser() throws Exception {
        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyT\":{},\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_TestModeOff_AdminUser"));
    }

    /**
     * RU: админ, включён режим тестирования
     * отправляет JSON'ом тестовую инфу
     */
    @Test @Tag("getInfo")
    @CustomUser
    void getInfo_whenGood_TestModeOn_AdminUser() throws Exception {
        prepareSchools();

        mockMvc.perform(get("/test/getInfo")
                .header(SecurityConfig.authTokenHeader, AppConfig.TEST_BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"bodyT\":{\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}}},\"bodyS\":{\"checkbox_debug\":true,\"checkbox_test\":true}}"))
            .andDo(defaultSwaggerDocs(getInfo_Summary, "getInfo_whenGood_TestModeOn_AdminUser"));
    }

    /**
     * RU: создаём случайные учебные центры
     */
    private void prepareSchools() {
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        initDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(usersTest);
        when(schoolRepository.findAllById(any())).thenReturn(schools);
        initDBService.getSchools().add(0L);
        when(lessonRepository.uniqTeachersUBySchool(anyLong())).thenReturn(usersTest);
    }
}