package ru.services;

import com.github.javafaker.Faker;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.Main;
import ru.controllers.CallInterface;
import ru.controllers.SSEController;
import ru.controllers.main.SettingsController;
import ru.controllers.people.StudentsController;
import ru.controllers.people.TeachersController;
import ru.controllers.school.analytics.KidJournalController;
import ru.controllers.school.analytics.ScheduleController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Lesson;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.school.LessonRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.db.DBService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.DAYS;

/** RU: главный сервис. Позволяет получить доступ к другим сервисам,
 * также имеет некоторый функционал в основном связанный с работой JSON */
@Slf4j
@RequiredArgsConstructor
@Getter
@Service public class MainService {
    private final DBService dbService;
    private final LessonRepository lessonRepository;

    /** RU: Формат даты, к которой легко обратиться */
    public final static DateFormat df = new SimpleDateFormat("dd.MM.yy");

    /** RU: Формат даты, к которой легко обратиться */
    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yy");

    /** RU: Вкл/выкл режима подробного описания ошибок */
    public static boolean debug = true;

    /** RU: Вкл/выкл режима генерации тестовых данных */
    public static boolean test = true;

    /** RU: Глобальные подписки, для авторизации и Server Sent Events*/
    public static final Map<UUID, SubscriberDTO> subscriptions = new ConcurrentHashMap<>();

    /** RU: инициирует побочные сервисы */
    public void postConstruct() {
        if(test) {
            subscriptions.put(UUID.fromString("9693b2a1-77bb-4426-8045-9f9b4395d454"), new SubscriberDTO("nm12"));
        }
    }

    public void getDbService() {
//        return dbService;
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
     * @see StudentsController#getInfo(SubscriberDTO, CustomToken)  Пример использования */
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
     * @see TeachersController#getTeachers(CustomToken, SubscriberDTO)   Пример использования */
    @SuppressWarnings("JavadocReference")
    public void teachersBySchool(School school, JsonWriter wrtr) throws Exception {
        wrtr.name("nt").beginObject().name("tea").beginObject();
        usersByList(school.getTeachers(), true, wrtr);
        wrtr.endObject().endObject();
        List<Object[]> lessU = lessonRepository.uniqTeachersLBySchool(school.getId());
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
                usersByList(mapU.get(subName), wrtr, true);
                wrtr.endObject().endObject();
                i++;
            }
        }
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Новая версия с пустым JSON
     * @see SettingsController#getSettings(SubscriberDTO)  Пример использования */
    public ResponseEntity getObjR(CallInterface callable, JsonTreeWriter wrtr, HttpStatus stat) {
        return getObjR(callable, wrtr, stat, true);
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Новая версия с не пустым JSON
     * @see SettingsController#getSettings(SubscriberDTO)  Пример использования */
    public ResponseEntity getObjR(CallInterface callable, JsonTreeWriter wrtr, HttpStatus stat, boolean nul) {
        var obj = getObj(callable, wrtr, !stat.isError());
        ResponseEntity.BodyBuilder build;
        if(obj == null && !stat.isError()) {
            build = ResponseEntity.internalServerError();
        } else {
            build = ResponseEntity.status(stat);
        }
        return nul ? build.build() : build.body(obj);
    }

    /** RU: завершает JSON и выводит его в консоль, выполняя функцию.<br>
     * Старая версия
     * @see SettingsController#getSettings(SubscriberDTO)  Пример использования */
    private JsonObject getObj(CallInterface callable, JsonTreeWriter wrtr, boolean isNotError) {
        JsonObject ans = null;
        try {
            wrtr.endObject();
            ans = wrtr.get().getAsJsonObject();
            log.debug("dsf" + ans);
            wrtr.close();
        } catch (Exception e) {
            isNotError = Main.excp(e);
        }
        if (ans == null || !isNotError) {
            return null;
        }
        callable.call(ans);
        return ans;
    }

    /** RU: инициализация JSON для метода.<br>
     * Новая версия
     * @param data JSON данные клиента
     * @param type POST/GET/PATCH...
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see SettingsController#getSettings(SubscriberDTO)  Пример использования */
    public JsonTreeWriter init(String data, String type) throws Exception {
        log.info(type + "! " + data);
        final JsonTreeWriter wrtr = new JsonTreeWriter();
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
     * @see ScheduleController#getSchedule(Long, SubscriberDTO, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
    public void getShedule(String nameWrtr, User user, JsonTreeWriter wrtr, Long gId) throws Exception {
        final Long schId = dbService.getFirstRole(user.getRoles()).getYO().getId();
        List<Lesson> lessons;
        if (user.getSelRole() == Roles.TEACHER) {
            lessons = lessonRepository.findBySchoolIdAndTeacherId(schId, user.getId());
        } else {
            lessons = lessonRepository.findBySchoolIdAndGrpId(schId, gId);
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
            if (user.getSelRole() == Roles.TEACHER) {
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
     * @see KidJournalController#getInfo(SubscriberDTO, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
    public Period getActualPeriodBySchool(School school) {
        try {
            long now = DAYS.toMillis(LocalDate.now().toEpochDay());
            for (Period per : school.getPeriods()) {
                if (now >= df.parse(per.getDateN()).getTime() && now <= df.parse(per.getDateK()).getTime()) {
                    return per;
                }
            }
        } catch (ParseException e) {
            Main.excp(e);
        }
        return null;
    }

    /** @return В оригинале рандомится с нижним подчёркиванием, заменяем на дефис*/
    public static String getRandomUsername(Faker faker){
        return faker.internet().slug().replace('_', '-');
    }

    /** RU: изменияет Server Sent Events пользователя и устанавливает ему UUID
     * @see SSEController#start(String)   */
    public static void setSSE(SubscriberDTO subscriber, SseEmitter SSE, UUID uuid) {
        subscriber.setSSE(SSE);
        if(SSE == null) return;

        subscriber.setSSEComplete(false);
        SSE.onCompletion(() -> {
            onCloseSSE(subscriber, uuid, "onCompletion");
        });
        SSE.onTimeout(() -> {
            SSE.complete();
            onCloseSSE(subscriber, uuid, "onTimeout");
        });
    }

    private static void onCloseSSE(SubscriberDTO subscriber, UUID uuid, String reason) {
        if(subscriber.getLogin() == null) {
            subscriptions.remove(uuid);
            log.debug("subscription " + uuid + " was closed from " + reason);
        } else {
            subscriber.setSSEComplete(true);
            log.debug("subscription " + uuid + " was noclosed from " + reason + " " + subscriber.getLogin());
        }
    }
}