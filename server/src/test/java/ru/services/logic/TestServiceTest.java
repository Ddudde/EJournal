package ru.services.logic;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.data.DAO.Syst;
import ru.data.DTO.controller.test.TestOutDTO;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.services.db.RandomizeService;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.TestUtils.schools;
import static utils.TestUtils.usersTest;

@ExtendWith(MockitoExtension.class)
public class TestServiceTest {
    private static final Gson gson = new Gson();
    private static final TestUtils testUtils = new TestUtils();

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private RandomizeService randomizeService;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestService(randomizeService, schoolRepository, lessonRepository);
    }

    /** RU: общий сценарий тестирования */
    private void getTestInfo_run(String expected) {
        final TestOutDTO dto = testService.getTestInfo();
        assertEquals(expected, gson.toJson(dto));
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("getTestInfo")
    void getTestInfo_whenEmpty() {
        getTestInfo_run("{\"bodyT\":{}}");
    }

    /** RU: подаёт список из случайных школ и должен вернуть заполненный JSON */
    @Test @Tag("getTestInfo")
    void getTestInfo_whenGood() {
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);

        randomizeService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(usersTest);
        when(lessonRepository.uniqTeachersUBySchool(anyLong())).thenReturn(usersTest);
        when(schoolRepository.findAllById(any())).thenReturn(schools);
        randomizeService.getSchools().add(0L);

        getTestInfo_run("{\"bodyT\":{\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"groups\":{\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}},\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}}}}}");
    }
}