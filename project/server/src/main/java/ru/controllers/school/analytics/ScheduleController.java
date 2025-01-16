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
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Lesson;
import ru.data.DAO.school.School;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.util.Comparator;
import java.util.List;

import static ru.Main.datas;

/** RU: Контроллер для управления/просмотра расписания + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ScheduleController">http://localhost:9001/swagger/htmlSwag/#/ScheduleController</a>
 * </pre>
 * @see Subscriber */
@Slf4j
@RequestMapping("/schedule")
@RequiredArgsConstructor
@RestController public class ScheduleController {

    /** RU: добавление урока + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addLesson")
    public ResponseEntity<Void> addLesson(@RequestBody DataSchedule body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[POST] /addLesson");
        final Group group = datas.getDbService().groupById(body.group);
        if(group == null) return ResponseEntity.notFound().build();

        final Long schId = Long.parseLong(sub.getLvlSch()),
            teaId = body.obj.getAsJsonObject("prepod").get("id").getAsLong();
        final School school = datas.getDbService().schoolById(schId);
        final Lesson lesson = new Lesson();
        final User teaU = datas.getDbService().userById(teaId);
        applyToTeacher(teaU, lesson, school);
        lesson.setNameSubject(body.obj.get("name").getAsString());
        lesson.setKab(body.obj.get("cabinet").getAsString());
        lesson.setGrp(group);
        lesson.setSchool(school);
        lesson.setDayWeek(body.day);
        final List<Lesson> lessons = datas.getDbService().getLessonRepository().findBySchoolIdAndGrpIdAndDayWeek(school.getId(), group.getId(), body.day);
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        if(lessons.isEmpty()) {
            lesson.setNumLesson(0);
        } else {
            lesson.setNumLesson(lessons.get(lessons.size()-1).getNumLesson()+1);
        }
        datas.getDbService().getLessonRepository().saveAndFlush(lesson);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);
        wrtr.name("bodyT").beginObject();
        datas.teachersBySchool(school, wrtr);
        wrtr.endObject();
        body.obj.addProperty("group", group.getName());
        wrtr.name("day").value(body.day)
            .name("les").value(lesson.getNumLesson());
        return datas.getObjR(ans -> {
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
        if(!teaU.getRoles().get(Roles.TEACHER).getSubjects().contains(lesson.getNameSubject())) {
            teaU.getRoles().get(Roles.TEACHER).getSubjects().add(lesson.getNameSubject());
            datas.getDbService().getUserRepository().saveAndFlush(teaU);
        }
    }

    /** RU: отправляет данные о расписании для группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @GetMapping("/getSchedule/{grId}")
    public ResponseEntity<JsonObject> getSchedule(@PathVariable Long grId, @AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getSchedule");
        final var ref = new Object() {
            Group group = null;
        };
        if(user.getSelRole() == Roles.KID) {
            ref.group = user.getSelecRole().getGrp();
        } else if(user.getSelRole() == Roles.PARENT) {
            final User kidU = datas.getDbService().userById(user.getSelKid());
            if(kidU != null) {
                ref.group = kidU.getRoles().get(Roles.KID).getGrp();
            }
        } else if(user.getSelRole() == Roles.HTEACHER) {
            ref.group = datas.getDbService().groupById(grId);
        }
        datas.getShedule("body", user, wrtr, ref.group != null ? ref.group.getId() : null);
        return datas.getObjR(ans -> {
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
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        log.info("[GET] /getInfo");
        final User user = sub.getUser();
        final School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.SCHEDULE, school.getId() +"", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп и учителей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('HTEACHER') OR hasAuthority('TEACHER'))""")
    @GetMapping("/getInfoToHT")
    public ResponseEntity<JsonObject> getInfoForHTeacherOrTEACHER(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoToHT");
        wrtr.name("bodyG").beginObject();
        final Long firstG = datas.groupsBySchoolOfUser(user, wrtr);
        final School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        wrtr.name("firstG").value(firstG)
            .name("bodyT").beginObject();
        datas.teachersBySchool(school, wrtr);
        wrtr.endObject();
        return datas.getObjR(ans -> {
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