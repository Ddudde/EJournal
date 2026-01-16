package ru.services.logic;

import com.google.gson.Gson;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.data.DAO.school.School;
import ru.data.DTO.service.school.TeacherServiceDTO;
import ru.data.reps.school.LessonRepository;
import ru.services.data.UserService;
import ru.services.db.DBService;
import ru.services.db.IDBService;
import ru.services.logic.people.TeacherService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.TestUtils.namesSubj;
import static utils.TestUtils.usersTest;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {
    protected static final Gson gson = new Gson();
    private final IDBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LessonRepository lessonRepository;

    @Spy
    private UserService userService = new UserService(dbService, null, null);

    @InjectMocks
    private TeacherService teacherService;

    /** RU: общий сценарий тестирования */
    private void teachersBySchool_run(String expected, School school) {
        final TeacherServiceDTO dto = teacherService.teachersBySchool(school);
        assertEquals(expected, gson.toJson(dto));
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test
    @Tag("teachersBySchool")
    void teachersBySchool_whenEmpty(@Mock School school) {
        when(school.getTeachers()).thenReturn(new ArrayList<>());
        when(lessonRepository
            .uniqTeachersLBySchool(anyLong())).thenReturn(new ArrayList<>());

        teachersBySchool_run("{\"nt\":{}}", school);
    }

    /** RU: подаёт список из учителей ещё не участвовающих в программе обучения и список дисциплин с их учителями.
     * Должен вернуть заполненный JSON */
    @Test @Tag("teachersBySchool")
    void teachersBySchool_whenGood(@Mock School school) {
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
        when(lessonRepository
            .uniqTeachersLBySchool(anyLong())).thenReturn(teachersBySchool);
        when(dbService.userById(0L)).thenReturn(usersTest.get(0));
        when(dbService.userById(1L)).thenReturn(usersTest.get(1));
        when(dbService.userById(2L)).thenReturn(usersTest.get(2));
        when(dbService.userById(3L)).thenReturn(usersTest.get(3));
        when(dbService.userById(4L)).thenReturn(usersTest.get(4));

        teachersBySchool_run("{\"nt\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"},\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}}},\"body\":{\"0\":{\"tea\":{\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"name\":\"Химия\"},\"1\":{\"tea\":{\"9764\":{\"name\":\"Силин А.К.\",\"login\":\"facere_a\"}},\"name\":\"Физика\"},\"2\":{\"tea\":{\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"},\"1705\":{\"name\":\"Дроздов А.А.\",\"login\":\"debitis_accusantium\"},\"3225\":{\"name\":\"Никифорова Н.А.\",\"login\":\"numquam_nobis\"}},\"name\":\"Математика\"},\"3\":{\"tea\":{\"1840\":{\"name\":\"Пестов Л.А.\",\"login\":\"sed_commodi\"}},\"name\":\"Русский Яз.\"},\"4\":{\"tea\":{\"3872\":{\"name\":\"Якушева А.О.\",\"login\":\"esse_et\"}},\"name\":\"Англ. Яз.\"}}}",
            school);
    }
}