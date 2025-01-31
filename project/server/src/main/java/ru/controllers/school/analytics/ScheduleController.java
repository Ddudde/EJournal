package ru.controllers.school.analytics;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.controllers.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Lesson;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;

import java.util.Comparator;
import java.util.List;

/** RU: Контроллер для управления/просмотра расписания + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ScheduleController">http://localhost:9001/swagger/htmlSwag/#/ScheduleController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/schedule")
@RequiredArgsConstructor
@RestController public class ScheduleController {
    private final MainService mainService;
    private final DBService dbService;
    private final SchoolRepository schoolRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    /** RU: добавление урока + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addLesson")
    public ResponseEntity<Void> addLesson(@RequestBody DataSchedule body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[POST] /addLesson");
        final Group group = dbService.groupById(body.group);
        if(group == null) return ResponseEntity.notFound().build();

        final Long schId = Long.parseLong(sub.getLvlSch()),
            teaId = body.obj.getAsJsonObject("prepod").get("id").getAsLong();
        final School school = dbService.schoolById(schId);
        final Lesson lesson = new Lesson();
        final User teaU = dbService.userById(teaId);
        applyToTeacher(teaU, lesson, school);
        lesson.setNameSubject(body.obj.get("name").getAsString());
        lesson.setKab(body.obj.get("cabinet").getAsString());
        lesson.setGrp(group);
        lesson.setSchool(school);
        lesson.setDayWeek(body.day);
        final List<Lesson> lessons = lessonRepository.findBySchoolIdAndGrpIdAndDayWeek(school.getId(), group.getId(), body.day);
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        if(lessons.isEmpty()) {
            lesson.setNumLesson(0);
        } else {
            lesson.setNumLesson(lessons.get(lessons.size()-1).getNumLesson()+1);
        }
        lessonRepository.saveAndFlush(lesson);
        schoolRepository.saveAndFlush(school);
        wrtr.name("bodyT").beginObject();
        mainService.teachersBySchool(school, wrtr);
        wrtr.endObject();
        body.obj.addProperty("group", group.getName());
        wrtr.name("day").value(body.day)
            .name("les").value(lesson.getNumLesson());
        return mainService.getObjR(ans -> {
            ans.add("body", body.obj);
            if(teaU != null) {
                SSEController.sendEventFor("addLessonC", ans, TypesConnect.SCHEDULE, sub.getLvlSch(), "main", "tea", teaU.getId()+"");
            }
            SSEController.sendEventFor("addLessonC", ans, TypesConnect.SCHEDULE, sub.getLvlSch(), "main", "ht", "main");
            SSEController.sendEventFor("addLessonC", ans, TypesConnect.SCHEDULE, sub.getLvlSch(), group.getId()+"", "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    private void applyToTeacher(User teaU, Lesson lesson, School school) {
        if(teaU == null) return;
        lesson.setTeacher(teaU);
        if(!ObjectUtils.isEmpty(school.getTeachers()) && school.getTeachers().contains(teaU)) {
            //удаление из списка учителей не вписанных в расписание
            school.getTeachers().remove(teaU);
        }
        if(!teaU.getRole(Roles.TEACHER).getSubjects().contains(lesson.getNameSubject())) {
            teaU.getRole(Roles.TEACHER).getSubjects().add(lesson.getNameSubject());
            userRepository.saveAndFlush(teaU);
        }
    }

    /** RU: отправляет данные о расписании для группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getSchedule/{grId}")
    public ResponseEntity<JsonObject> getSchedule(@PathVariable Long grId, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getSchedule");
        final var ref = new Object() {
            Group group = null;
        };
        if(user.getSelRole() == Roles.KID) {
            ref.group = user.getSelecRole().getGrp();
        } else if(user.getSelRole() == Roles.PARENT) {
            final User kidU = dbService.userById(user.getSelKid());
            if(kidU != null) {
                ref.group = kidU.getRole(Roles.KID).getGrp();
            }
        } else if(user.getSelRole() == Roles.HTEACHER) {
            ref.group = dbService.groupById(grId);
        }
        mainService.getShedule("body", user, wrtr, ref.group != null ? ref.group.getId() : null);
        return mainService.getObjR(ans -> {
            String group = null;
            if(user.getSelRole() == Roles.KID || user.getSelRole() == Roles.PARENT) {
                group = ref.group.getId()+"";
            }
            SSEController.changeSubscriber(auth.getUUID(), null, null, null, group, null, null);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        log.info("[GET] /getInfo");
        final User user = dbService.userById(sub.getUserId());
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.SCHEDULE, school.getId() +"", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп и учителей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('HTEACHER') OR hasAuthority('TEACHER'))""")
    @GetMapping("/getInfoToHT")
    public ResponseEntity<JsonObject> getInfoForHTeacherOrTEACHER(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfoToHT");
        wrtr.name("bodyG").beginObject();
        final Long firstG = mainService.groupsBySchoolOfUser(user, wrtr);
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        wrtr.name("firstG").value(firstG)
            .name("bodyT").beginObject();
        mainService.teachersBySchool(school, wrtr);
        wrtr.endObject();
        return mainService.getObjR(ans -> {
            String role = "main", teacherId = "main";
            if(user.getSelRole() == Roles.HTEACHER) role = "ht";
            if(user.getSelRole() == Roles.TEACHER) {
                role = "tea";
                teacherId = user.getId()+"";
            }
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.SCHEDULE, school.getId() +"", "main", role, teacherId);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые ScheduleController в методах
     * @see ScheduleController */
    @ToString
    @RequiredArgsConstructor
    static final class DataSchedule {
        public final JsonObject obj;
        public final Long group;
        public final int day;
    }
}