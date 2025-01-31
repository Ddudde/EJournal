package ru.services.db;

import com.google.gson.internal.bind.JsonTreeWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
import ru.services.MainService;
import utils.TestUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.TestUtils.schools;
import static utils.TestUtils.usersTest;

@ExtendWith(MockitoExtension.class)
public class IniDBServiceTest {
    private static Method getUsersTM;
    private final TestUtils testUtils = new TestUtils();
    private final DBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);

    @Mock
    private SettingUserRepository settingUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private ContactsRepository contactsRepository;

    @Mock
    private SystRepository systRepository;

    @Mock
    private DayRepository dayRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private MarkRepository markRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PeriodRepository periodRepository;

    @Mock
    private RequestRepository requestRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    @Spy
    private MainService mainService = new MainService(dbService, lessonRepository);

    @InjectMocks
    private IniDBService iniDBService;

    @BeforeAll
    static void beforeAll() throws NoSuchMethodException {
        getUsersTM = IniDBService.class.getDeclaredMethod("getUsersT", JsonTreeWriter.class, List.class);
        getUsersTM.setAccessible(true);
    }

    @BeforeEach
    void setUp() {
        mainService.postConstruct();
    }

    @Test
    void testOff_whenGood() {
        when(newsRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(periodRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(systRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(settingUserRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(userRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(contactsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(schoolRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(groupRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(lessonRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(markRepository.saveAndFlush(any()))
                .then(invocation -> invocation.getArguments()[0]);
        when(dayRepository.saveAndFlush(any()))
                .then(invocation -> invocation.getArguments()[0]);

        iniDBService.testOn();
        iniDBService.testOff();

        assertNull(iniDBService.getSyst());
    }

    @Test
    void testOn_whenGood() {
        when(newsRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(periodRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(systRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(settingUserRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(userRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(contactsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(schoolRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(groupRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(lessonRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(markRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dayRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);

        iniDBService.testOn();

        assertNotNull(iniDBService.getSyst());
    }

    /** RU: общий сценарий тестирования */
    private void getTestInfo_run(String expected) throws Exception {
        final JsonTreeWriter wrtr = new JsonTreeWriter();
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
        Syst syst = mock(Syst.class, Answers.RETURNS_DEEP_STUBS);
        iniDBService.setSyst(syst);
        when(syst.getAdmins()).thenReturn(usersTest);
        when(lessonRepository.uniqTeachersUBySchool(anyLong())).thenReturn(usersTest);
        when(schoolRepository.findAllById(any())).thenReturn(schools);
        iniDBService.getSchools().add(0L);

        getTestInfo_run("{\"bodyT\":{\"testPassword\":null,\"admins\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"schools\":{\"9600\":{\"name\":\"Лицей №3293\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}}}},\"1137\":{\"name\":\"Гимназия №2246\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}}}},\"2903\":{\"name\":\"Школа №217\",\"hteachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"teachers\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"groups\":{\"2323\":{\"name\":\"1А\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"3456\":{\"name\":\"1Б\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}},\"4354\":{\"name\":\"1В\",\"kids\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}},\"parents\":{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null}}}}}}}}");
    }

    /** RU: общий сценарий тестирования */
    private void getUsersT_run(List<User> admins, String expected) throws InvocationTargetException, IllegalAccessException, IOException {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        getUsersT(wrtr, admins);
        wrtr.endObject();
        assertEquals(expected, wrtr.get().getAsJsonObject().toString());
    }

    private void getUsersT(JsonTreeWriter wrtr, List<User> users) throws InvocationTargetException, IllegalAccessException {
        getUsersTM.invoke(iniDBService, wrtr, users);
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("getUsersT")
    void getUsersT_whenEmpty() throws InvocationTargetException, IllegalAccessException, IOException {
        getUsersT_run(new ArrayList<>(), "{}");
    }

    /** RU: подаёт список из случайных пользователей и должен вернуть заполненный JSON */
    @Test @Tag("getUsersT")
    void getUsersT_whenGood() throws InvocationTargetException, IllegalAccessException, IOException {
        getUsersT_run(usersTest,
            "{\"3872\":{\"fio\":\"Якушева А.О.\",\"login\":\"esse_et\",\"code\":null},\"1705\":{\"fio\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\",\"code\":null},\"1840\":{\"fio\":\"Пестов Л.А.\",\"login\":\"sed_commodi\",\"code\":null},\"3225\":{\"fio\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\",\"code\":null},\"9764\":{\"fio\":\"Силин А.К.\",\"login\":\"facere_a\",\"code\":null}}"
        );
    }
}