package ru.mirea.services;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
import ru.mirea.controllers.CallInterface;
import ru.mirea.controllers.main.SettingsController;
import ru.mirea.controllers.people.StudentsController;
import ru.mirea.controllers.people.TeachersController;
import ru.mirea.controllers.school.analytics.JournalController;
import ru.mirea.controllers.school.analytics.ScheduleController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.data.models.school.Period;
import ru.mirea.data.models.school.School;
import ru.mirea.security.CustomToken;
import ru.mirea.services.db.DBService;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.DAYS;

/** RU: главный сервис. Позволяет получить доступ к другим сервисам,
 * также имеет некоторый функционал в основном связанный с работой JSON
 * <pre>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Тестирование
 * </pre> */
@RequiredArgsConstructor
@Getter
@Service public class MainService {

    /** RU: объект отправляемый при ошибках. {"error":true}*/
    private final JsonObject errObj = new JsonObject();

    /** RU: объект отправляемый при пустых телах. {}*/
    private final JsonObject nullObj = new JsonObject();

    private final PushService pushService;

    private final DBService dbService;

    private final EmailService emailService;

    /** RU: Глобальные подписки, для авторизации и Server Sent Events*/
    public final Map<UUID, Subscriber> subscriptions = new ConcurrentHashMap<>();

    /** RU: инициирует побочные сервисы */
    @PostConstruct
    public void postConstruct() {
        Main.datas = this;
        errObj.addProperty("error", true);
    }

    /** RU: готовит JSON с данными списка пользователей.
     * <pre>
     * user.ID : {
     *     "name" : "FIO",
     *     "login",
     *     "link",
     * }
     * </pre>
     * @param lin добавляет свойство "link" в объект
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see #teachersBySchool(School, JsonWriter) Пример использования */
    private void usersByList(List<Long> list, JsonWriter wrtr, boolean lin) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Long i : list) {
            User objU = dbService.userById(i);
            if (objU == null) continue;
            wrtr.name(i + "").beginObject()
                .name("name").value(objU.getFio());
            if (!ObjectUtils.isEmpty(objU.getUsername())) {
                wrtr.name("login").value(objU.getUsername());
            }
            if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
                wrtr.name("link").value(objU.getCode());
            }
            wrtr.endObject();
        }
    }

    /** RU: готовит JSON с данными списка пользователей.
     * <pre>
     * user.ID : {
     *     "name" : "FIO",
     *     "login",
     *     "link",
     * }
     * </pre>
     * @param lin добавляет свойство "link" в объект
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see #teachersBySchool(School, JsonWriter) Пример использования */
    public void usersByList(List<User> list, boolean lin, JsonWriter wrtr) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (User objU : list) {
            if (objU == null) continue;
            wrtr.name(objU.getId() + "").beginObject()
                .name("name").value(objU.getFio());
            if (!ObjectUtils.isEmpty(objU.getUsername())) {
                wrtr.name("login").value(objU.getUsername());
            }
            if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
                wrtr.name("link").value(objU.getCode());
            }
            wrtr.endObject();
        }
    }

    /** RU: готовит JSON с данными групп школы.
     * <pre>
     * bodyG : {
     *     group.ID : group.name
     * }
     * </pre>
     * @return ID первой группы
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see StudentsController#getInfo(ru.mirea.controllers.people.DataStudents) Пример использования */
    @SuppressWarnings("JavadocReference")
    public Long groupsBySchoolOfUser(User user, JsonWriter wrtr) throws Exception {
        Long first = null;
        if (user != null) {
            School school = dbService.getFirstRole(user.getRoles()).getYO();
            if (!ObjectUtils.isEmpty(school.getGroups())) {
                first = school.getGroups().get(0).getId();
                for (Group gr : school.getGroups()) {
                    wrtr.name(gr.getId() + "").value(gr.getName());
                }
            }
        }
        wrtr.endObject();
        return first;
    }

    /** RU: готовит JSON с данными списка учителей.
     * <pre>
     * nt : {
     *     tea : {{@link #usersByList}}
     * },
     * intNumSubject : {
     *     "name",
     *     tea : {{@link #usersByList}}
     * }
     * nt - учителя принадлежащие школе, но ещё не прикреплённые к дисциплинам.
     * Дальше определённая дисциплина и учителя, которые её преподают
     * </pre>
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see TeachersController#getTeachers(ru.mirea.controllers.people.DataTeachers)  Пример использования */
    @SuppressWarnings("JavadocReference")
    public void teachersBySchool(School school, JsonWriter wrtr) throws Exception {
        wrtr.name("nt").beginObject().name("tea").beginObject();
        usersByList(school.getTeachers(), true, wrtr);
        wrtr.endObject().endObject();
        List<Object[]> lessU = dbService.getLessonRepository().uniqTeachersLBySchool(school.getId());
        if (!ObjectUtils.isEmpty(lessU)) {
            Map<String, List<Long>> mapU = lessU.stream().collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
            ));
            int i = 0;
            for (String subName : mapU.keySet()) {
                wrtr.name(i + "").beginObject()
                    .name("name").value(subName)
                    .name("tea").beginObject();
                if (mapU.containsKey(subName)) {
                    usersByList(mapU.get(subName), wrtr, true);
                }
                wrtr.endObject().endObject();
                i++;
            }
        }
        wrtr.endObject();
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Новая версия с пустым JSON
     * @see SettingsController#getSettings(CustomToken) Пример использования */
    public ResponseEntity getObjR(CallInterface callable, JsonTreeWriter wrtr, HttpStatus stat) {
        return getObjR(callable, wrtr, stat, true);
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Новая версия с не пустым JSON
     * @see SettingsController#getSettings(CustomToken) Пример использования */
    public ResponseEntity getObjR(CallInterface callable, JsonTreeWriter wrtr, HttpStatus stat, boolean nul) {
        var obj = getObj(callable, wrtr, !stat.isError());
        ResponseEntity.BodyBuilder build;
        if(obj == errObj && !stat.isError()) {
            build = ResponseEntity.internalServerError();
        } else {
            build = ResponseEntity.status(stat);
        }
        return nul ? build.build() : build.body(obj);
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Старая версия
     * @see SettingsController#getSettings(CustomToken) Пример использования */
    public JsonObject getObj(CallInterface callable, JsonTreeWriter wrtr, boolean bol) {
        JsonObject ans = null;
        try {
            wrtr.endObject();
            ans = wrtr.get().getAsJsonObject();
            System.out.println("dsf" + ans);
            wrtr.close();
        } catch (Exception e) {
            bol = Main.excp(e);
        }
        if (ans != null && bol) {
            callable.call(ans);
        } else {
            ans = errObj;
        }
        return ans;
    }

    /** RU: инициализация JSON для метода.<br>
     * Старая версия */
    public JsonTreeWriter init(String data) throws Exception {
        JsonTreeWriter wrtr = init(data, "Post");
        wrtr.name("error").value(false);
        return wrtr;
    }

    /** RU: инициализация JSON для метода.<br>
     * Новая версия
     * @param data JSON данные клиента
     * @param type POST/GET/PATCH...
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see SettingsController#getSettings(CustomToken) Пример использования */
    public JsonTreeWriter init(String data, String type) throws Exception {
        System.out.println(type + "! " + data);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject();
        return wrtr;
    }

    /** RU: готовит JSON с данными расписания для педагога или группы
     * <pre>
     * body : {
     *     intDayWeek : {
     *         "lessons" : {
     *             intNumLesson : {
     *                 "name",
     *                 "cabinet",
     *                 "group",
     *                 "prepod" : {
     *                     "id",
     *                     "name"
     *                 }
     *             }
     *         }
     *     }
     * }
     * </pre>
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see ScheduleController#getSchedule(ru.mirea.controllers.analytics.DataSchedule) Пример использования */
    @SuppressWarnings("JavadocReference")
    public void getShedule(String nameWrtr, User user, JsonTreeWriter wrtr, Long gId) throws Exception {
        Long schId = dbService.getFirstRole(user.getRoles()).getYO().getId();
        List<Lesson> lessons;
        if (user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
            lessons = dbService.getLessonRepository().findBySchoolIdAndTeacherId(schId, user.getId());
        } else {
            lessons = dbService.getLessonRepository().findBySchoolIdAndGrpId(schId, gId);
        }
        wrtr.name(nameWrtr).beginObject();
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        int k1 = -1;
        for (Lesson les : lessons) {
            if (k1 != les.getDayWeek()) {
                if (k1 != -1) wrtr.endObject().endObject();
                k1 = les.getDayWeek();
                wrtr.name(k1 + "").beginObject()
                    .name("lessons").beginObject();
            }
            wrtr.name(les.getNumLesson() + "").beginObject();
            if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                wrtr.name("name").value(les.getNameSubject());
            }
            wrtr.name("cabinet").value(les.getKab());
            if (user.getSelRole() == 2L) {
                Group grp = les.getGrp();
                wrtr.name("group").value(grp.getName());
            } else {
                User teaU = les.getTeacher();
                wrtr.name("prepod").beginObject();
                if (teaU != null) {
                    wrtr.name("name").value(teaU.getFio())
                        .name("id").value(teaU.getId());
                }
                wrtr.endObject();
            }
            wrtr.endObject();
        }
        if (k1 != -1) wrtr.endObject().endObject();
        wrtr.endObject();
    }

    /** RU: исходя из заданных периодов в школе и актуальной даты
     * выбирается активный период
     * @see JournalController#getInfo(ru.mirea.controllers.analytics.DataJournal) Пример использования */
    @SuppressWarnings("JavadocReference")
    public Period getActualPeriodBySchool(School school) {
        try {
            long now = DAYS.toMillis(LocalDate.now().toEpochDay());
            for (Period per : school.getPeriods()) {
                if (now >= Main.df.parse(per.getDateN()).getTime() && now <= Main.df.parse(per.getDateK()).getTime()) {
                    return per;
                }
            }
        } catch (ParseException e) {
            Main.excp(e);
        }
        return null;
    }
}