package ru.mirea.services.db;

import com.google.gson.internal.bind.JsonTreeWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.MainService;
import utils.RandomUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IniDBServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DBService dbService;

    @InjectMocks
    private IniDBService iniDBService;

    @InjectMocks
    private MainService mainService;

    private static RandomUtils randomUtils;

    private static Method getUsersTM;

    @BeforeAll
    static void beforeAll() throws NoSuchMethodException {
        getUsersTM = IniDBService.class.getDeclaredMethod("getUsersT", JsonTreeWriter.class, List.class);
        getUsersTM.setAccessible(true);
        randomUtils = new RandomUtils();
    }

    @BeforeEach
    void setUp() {
        mainService.postConstruct();
    }

    @Test
    void testOff_whenGood() {
        when(dbService.getNewsRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.getContactsRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.getPeriodRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSystRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSettingUserRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getUserRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getNewsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getContactsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSchoolRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getGroupRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getLessonRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        iniDBService.testOn();
        iniDBService.testOff();
        assertNull(iniDBService.getSyst());
    }

    @Test
    void testOn_whenGood() {
        when(dbService.getNewsRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.getContactsRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.getPeriodRepository().saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSystRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSettingUserRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getRoleRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getUserRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getNewsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getContactsRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getSchoolRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getGroupRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dbService.getLessonRepository().saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        iniDBService.testOn();
        assertNotNull(iniDBService.getSyst());
    }

    /** RU: общий сценарий тестирования */
    private void getTestInfo_run(String expected) throws Exception {
        when(dbService.getLessonRepository().uniqTeachersUBySchool(anyLong())).thenReturn(randomUtils.usersTest);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        iniDBService.getTestInfo(wrtr);
        wrtr.endObject();
        assertEquals(expected, wrtr.get().getAsJsonObject().toString());
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("getTestInfo")
    void getTestInfo_whenEmpty() throws Exception {
        getTestInfo_run("{\"bodyT\":{\"admins\":{},\"schools\":{}}}");
    }

    /** RU: подаёт список из случайных школ и должен вернуть заполненный JSON */
    @Test @Tag("getTestInfo")
    void getTestInfo_whenGood() throws Exception {
        List<School> schools = new ArrayList<>(asList(
            randomUtils.getSchool(9600, "Лицей №3293", 2616, 4866, 4117),
            randomUtils.getSchool(1137, "Гимназия №2246", 3146, 4701, 5506),
            randomUtils.getSchool(2903, "Школа №217", 1973, 5375, 7100)
        ));
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(randomUtils.usersTest);
        iniDBService.getSchools().addAll(schools);
        getTestInfo_run("{\"bodyT\":{\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"groups\":{\"2616\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"4866\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"4117\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"groups\":{\"3146\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"4701\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"5506\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"groups\":{\"1973\":{\"name\":\"1A\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"5375\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}},\"7100\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null}}}}}}}}");
    }

    private void getUsersT(JsonTreeWriter wrtr, List<User> users) throws InvocationTargetException, IllegalAccessException {
        getUsersTM.invoke(iniDBService, wrtr, users);
    }

    /** RU: общий сценарий тестирования */
    private void getUsersT_run(List<User> admins, String expected) throws InvocationTargetException, IllegalAccessException, IOException {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        getUsersT(wrtr, admins);
        wrtr.endObject();
        assertEquals(expected, wrtr.get().getAsJsonObject().toString());
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("getUsersT")
    void getUsersT_whenEmpty() throws InvocationTargetException, IllegalAccessException, IOException {
        getUsersT_run(new ArrayList<>(), "{}");
    }

    /** RU: подаёт список из случайных пользователей и должен вернуть заполненный JSON */
    @Test @Tag("getUsersT")
    void getUsersT_whenGood() throws InvocationTargetException, IllegalAccessException, IOException {
        getUsersT_run(randomUtils.usersTest,
            "{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"pass\":\"k02o9ezp8tacrfp\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"pass\":\"9an3e5ykj8\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"pass\":\"zjitnc71x\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"pass\":\"hx45205la\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"pass\":\"2qiasp5nsk4mq7\",\"code\":null}}"
        );
    }
}