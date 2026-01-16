package ru.services.logic.school;

import com.google.gson.Gson;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Lesson;
import ru.data.DTO.service.school.ScheduleServiceDTO;
import ru.data.reps.school.LessonRepository;
import ru.security.user.Roles;
import ru.services.db.IDBService;
import ru.services.logic.school.analytics.ScheduleService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static utils.TestUtils.usersTest;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    protected static final Gson gson = new Gson();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IDBService dbService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LessonRepository lessonRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    /** RU: общий сценарий тестирования */
    private void getShedule_run(String expected, User user) {
        when(dbService.getFirstRole(anyMap()).getYO().getId()).thenReturn(1L);
        final ScheduleServiceDTO dto = scheduleService.getShedule(user, 1L);
        assertEquals(expected, gson.toJson(dto));
    }

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test
    @Tag("getShedule")
    void getShedule_whenEmpty(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) {
        List<Lesson> lessons = new ArrayList<>();
        when(user.getSelRole()).thenReturn(Roles.PARENT);
        when(lessonRepository
            .findBySchoolIdAndGrpId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"body\":{}}", user);
    }

    /** RU: подаёт список из уроков для определённой группы и должен вернуть заполненный JSON */
    @Test @Tag("getShedule")
    void getShedule_whenGood_byGroup(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) {
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
        when(lessonRepository
            .findBySchoolIdAndGrpId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"prepod\":{\"id\":9764,\"name\":\"Силин А.К.\"}},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"prepod\":{\"id\":3872,\"name\":\"Якушева А.О.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"prepod\":{\"id\":1705,\"name\":\"Дроздов А.А.\"}}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"prepod\":{\"id\":1840,\"name\":\"Пестов Л.А.\"}},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"prepod\":{\"id\":3225,\"name\":\"Никифорова Н.А.\"}}}}}}",
            user);
    }

    /** RU: подаёт список из уроков для определённого учителя и должен вернуть заполненный JSON */
    @Test @Tag("getShedule")
    void getShedule_whenGood_byTeacher(@Mock(answer = Answers.RETURNS_DEEP_STUBS) User user) {
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
        when(lessonRepository
            .findBySchoolIdAndTeacherId(anyLong(), anyLong())).thenReturn(lessons);
        getShedule_run("{\"body\":{\"1\":{\"lessons\":{\"0\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1283\",\"group\":\"1В\"},\"3\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1977\",\"group\":\"1А\"},\"4\":{\"name\":\"Математика\",\"cabinet\":\"1870\",\"group\":\"1В\"},\"5\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"640\",\"group\":\"1Б\"}}},\"3\":{\"lessons\":{\"0\":{\"name\":\"Англ. Яз.\",\"cabinet\":\"1098\",\"group\":\"1В\"},\"2\":{\"name\":\"Русский Яз.\",\"cabinet\":\"1660\",\"group\":\"1А\"},\"4\":{\"name\":\"Физика\",\"cabinet\":\"1837\",\"group\":\"1В\"}}},\"4\":{\"lessons\":{\"3\":{\"name\":\"Русский Яз.\",\"cabinet\":\"482\",\"group\":\"1Б\"},\"4\":{\"name\":\"Физика\",\"cabinet\":\"394\",\"group\":\"1Б\"}}}}}",
            user);
    }
}