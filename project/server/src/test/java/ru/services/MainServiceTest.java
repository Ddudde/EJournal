package ru.services;

import com.google.gson.internal.bind.JsonTreeWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.Lesson;
import ru.data.models.school.Period;
import ru.data.models.school.School;
import ru.security.user.Roles;
import ru.services.db.DBService;
import ru.services.db.IniDBService;
import utils.RandomUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static utils.RandomUtils.namesSubj;
import static utils.RandomUtils.usersTest;

@ExtendWith(MockitoExtension.class)
public class MainServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IniDBService iniDBService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DBService dbService;

    @InjectMocks
    private MainService mainService;

    private final RandomUtils randomUtils = new RandomUtils();

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("groupsBySchoolOfUser")
    void groupsBySchoolOfUser_whenEmpty() throws Exception {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        mainService.groupsBySchoolOfUser(null, wrtr);
        assertEquals("{}",
            wrtr.get().getAsJsonObject().toString());
    }

    /** RU: подаёт список из случайных групп должен вернуть заполненный JSON */
    @Test @Tag("groupsBySchoolOfUser")
    void groupsBySchoolOfUser_whenGood(@Mock User user, @Mock(answer = Answers.RETURNS_DEEP_STUBS) School school) throws Exception {
        when(dbService.getFirstRole(anyMap()).getYO()).thenReturn(school);
        when(school.getGroups()).thenReturn(randomUtils.groups);

        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        assertEquals(2323L,
            mainService.groupsBySchoolOfUser(user, wrtr));
        assertEquals("{\"2323\":\"1А\",\"3456\":\"1Б\",\"4354\":\"1В\"}",
            wrtr.get().getAsJsonObject().toString());
    }

    /** RU: общий сценарий тестирования */
    private void teachersBySchool_run(String expected, School school) throws Exception {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        mainService.teachersBySchool(school, wrtr);
        wrtr.endObject();
        assertEquals(expected, wrtr.get().getAsJsonObject().toString());
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("teachersBySchool")
    void teachersBySchool_whenEmpty(@Mock School school) throws Exception {
        when(school.getTeachers()).thenReturn(new ArrayList<>());
        when(dbService.getLessonRepository().uniqTeachersLBySchool(anyLong())).thenReturn(new ArrayList<>());

        teachersBySchool_run("{\"nt\":{\"tea\":{}}}", school);
    }

    /** RU: подаёт список из учителей ещё не участвовающих в программе обучения и список дисциплин с их учителями.
     * Должен вернуть заполненный JSON */
    @Test @Tag("teachersBySchool")
    void teachersBySchool_whenGood(@Mock School school) throws Exception {
        List<Object[]> teachersBySchool = new ArrayList<>(asList(
            new Object[] {namesSubj[0], 0L},
            new Object[] {namesSubj[1], 1L},
            new Object[] {namesSubj[1], 2L},
            new Object[] {namesSubj[1], 3L},
            new Object[] {namesSubj[2], 2L},
            new Object[] {namesSubj[3], 3L},
            new Object[] {namesSubj[4], 4L}
        ));
        when(school.getTeachers()).thenReturn(usersTest);
        when(dbService.getLessonRepository().uniqTeachersLBySchool(anyLong())).thenReturn(teachersBySchool);
        when(dbService.userById(0L)).thenReturn(usersTest.get(0));
        when(dbService.userById(1L)).thenReturn(usersTest.get(1));
        when(dbService.userById(2L)).thenReturn(usersTest.get(2));
        when(dbService.userById(3L)).thenReturn(usersTest.get(3));
        when(dbService.userById(4L)).thenReturn(usersTest.get(4));

        teachersBySchool_run("{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"0\":{\"name\":\"Химия\",\"tea\":{\"3\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}},\"1\":{\"name\":\"Физика\",\"tea\":{\"4\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}}},\"2\":{\"name\":\"Математика\",\"tea\":{\"1\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"2\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"3\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}},\"3\":{\"name\":\"Русский Яз.\",\"tea\":{\"2\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}}},\"4\":{\"name\":\"Англ. Яз.\",\"tea\":{\"0\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}}}}",
            school);
    }

    /** RU: общий сценарий тестирования */
    private void getShedule_run(String expected, User user) throws Exception {
        when(dbService.getFirstRole(anyMap()).getYO().getId()).thenReturn(1L);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        mainService.getShedule("testShedule", user, wrtr, 1L);
        wrtr.endObject();
        assertEquals(expected, wrtr.get().getAsJsonObject().toString());
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("getShedule")
    void getShedule_whenEmpty(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) throws Exception {
        List<Lesson> lessons = new ArrayList<>();
        when(user.getSelRole()).thenReturn(Roles.PARENT);
        when(dbService.getLessonRepository().findBySchoolIdAndGrpId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"testShedule\":{}}",
            user);
    }

    /** RU: подаёт список из уроков для определённой группы и должен вернуть заполненный JSON */
    @Test @Tag("getShedule")
    void getShedule_whenGood_byGroup(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) throws Exception {
        List<Lesson> lessons = new ArrayList<>(asList(
            new Lesson(null, null, 1, 0, "1283", "Русский Яз.", usersTest.get(0)),
            new Lesson(null, null, 1, 3, "1977", "Англ. Яз.", usersTest.get(1)),
            new Lesson(null, null, 1, 4, "1870", "Математика", usersTest.get(2)),
            new Lesson(null, null, 1, 5, "640", "Англ. Яз.", usersTest.get(3)),
            new Lesson(null, null, 3, 0, "1098", "Англ. Яз.", usersTest.get(4)),
            new Lesson(null, null, 3, 2, "1660", "Русский Яз.", usersTest.get(0)),
            new Lesson(null, null, 3, 4, "1837", "Физика", usersTest.get(1)),
            new Lesson(null, null, 4, 3, "482", "Русский Яз.", usersTest.get(2)),
            new Lesson(null, null, 4, 4, "394", "Физика", usersTest.get(3))
        ));
        when(user.getSelRole()).thenReturn(Roles.PARENT);
        when(dbService.getLessonRepository().findBySchoolIdAndGrpId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"testShedule\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"name\":\"Силин А.К.\",\"id\":9764}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"name\":\"Якушева А.О.\",\"id\":3872}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"name\":\"Дроздов А.А.\",\"id\":1705}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"name\":\"Пестов Л.А.\",\"id\":1840}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"name\":\"Никифорова Н.А.\",\"id\":3225}}}}}}",
            user);
    }

    /** RU: подаёт список из уроков для определённого учителя и должен вернуть заполненный JSON */
    @Test @Tag("getShedule")
    void getShedule_whenGood_byTeacher(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) throws Exception {
        List<Lesson> lessons = new ArrayList<>(asList(
            new Lesson(null, new Group("1В"), 1, 0, "1283", "Русский Яз.", null),
            new Lesson(null, new Group("1А"), 1, 3, "1977", "Англ. Яз.", null),
            new Lesson(null, new Group("1В"), 1, 4, "1870", "Математика", null),
            new Lesson(null, new Group("1Б"), 1, 5, "640", "Англ. Яз.", null),
            new Lesson(null, new Group("1В"), 3, 0, "1098", "Англ. Яз.", null),
            new Lesson(null, new Group("1А"), 3, 2, "1660", "Русский Яз.", null),
            new Lesson(null, new Group("1В"), 3, 4, "1837", "Физика", null),
            new Lesson(null, new Group("1Б"), 4, 3, "482", "Русский Яз.", null),
            new Lesson(null, new Group("1Б"), 4, 4, "394", "Физика", null)
        ));
        when(user.getSelRole()).thenReturn(Roles.TEACHER);
        when(dbService.getLessonRepository().findBySchoolIdAndTeacherId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"testShedule\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"group\":\"1В\"},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"group\":\"1А\"},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"group\":\"1В\"},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"group\":\"1Б\"}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"group\":\"1В\"},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"group\":\"1А\"},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"group\":\"1В\"}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"group\":\"1Б\"},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"group\":\"1Б\"}}}}}",
            user);
    }

    /** RU: общий сценарий тестирования */
    private Period getActualPeriodBySchool_run(School school, LocalDate date) {
        when(school.getPeriods()).thenReturn(randomUtils.periods);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(LocalDate::now).thenReturn(date);
            return mainService.getActualPeriodBySchool(school);
        }
    }

    /** RU: имитирует 25 марта 2023 и должен вернуть NULL */
    @Test @Tag("getActualPeriodBySchool")
    void getActualPeriodBySchool_whenBad(@Mock School school) {
        LocalDate date = LocalDate.of(2023, 3, 25);
        assertNull(getActualPeriodBySchool_run(school, date));
    }

    /** RU: имитирует 25 марта 2024 и должен выбрать актуальной 3 четверть */
    @Test @Tag("getActualPeriodBySchool")
    void getActualPeriodBySchool_whenGood(@Mock School school) {
        LocalDate date = LocalDate.of(2024, 3, 25);
        assertEquals("III четверть",
            getActualPeriodBySchool_run(school, date).getName());
    }
}