package ru.mirea.controllers.analytics;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.school.dayOfWeek.DayOfWeek;
import ru.mirea.data.models.school.dayOfWeek.Lesson;
import ru.mirea.data.models.school.dayOfWeek.Subject;
import ru.mirea.services.ServerService;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Arrays.asList;

@RequestMapping("/schedule")
@NoArgsConstructor
@RestController public class ScheduleController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addLesson")
    public JsonObject addLesson(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            DayOfWeek dayOfWeek = null;
            Group group = null;
            Lesson lesson = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                ref.group = datas.groupById(body.group);
                if(ref.group != null) {
                    ref.lesson = new Lesson();
                    Long schId = Long.parseLong(subscriber.getLvlSch()),
                        teaId = body.obj.getAsJsonObject("prepod").get("id").getAsLong();
                    School school = datas.schoolById(schId);
                    Subject subject = datas.subjectByNameAndSchool(body.obj.get("name").getAsString(), schId);
                    if(subject == null) {
                        subject = new Subject(body.obj.get("name").getAsString(), schId, new ArrayList<>(asList(teaId)));
                        datas.getSubjectRepository().saveAndFlush(subject);
                        school.getSubjects().add(subject.getId());
                    }
                    ref.lesson.setSubject(subject.getId());
                    ref.lesson.setKab(body.obj.get("cabinet").getAsString());
                    User teaU = datas.userById(teaId);
                    Invite teaI = datas.inviteById(teaId);
                    if(teaU != null) {
                        ref.lesson.setTeacher(teaId);
                        if(!ObjectUtils.isEmpty(school.getTeachers())
                                && school.getTeachers().contains(teaId)){
                            school.getTeachers().remove(teaId);
                        }
                        teaU.getRoles().get(2L).getSubjects().add(subject.getId());
                        datas.getUserRepository().saveAndFlush(teaU);
                        subject.getTeachers().add(teaU.getId());
                        datas.getSubjectRepository().saveAndFlush(subject);
                    } else if(teaI != null){
                        ref.lesson.setTeacherInv(teaId);
                        if(!ObjectUtils.isEmpty(school.getTeachersInv())
                                && school.getTeachersInv().contains(teaId)){
                            school.getTeachersInv().remove(teaId);
                        }
                        teaI.getRole().get(2L).getSubjects().add(subject.getId());
                        datas.getInviteRepository().saveAndFlush(teaI);
                        subject.getTeachersInv().add(teaI.getId());
                        datas.getSubjectRepository().saveAndFlush(subject);
                    }
                    datas.getLessonRepository().saveAndFlush(ref.lesson);
                    datas.getSchoolRepository().saveAndFlush(school);
                    if(body.dayId != null) {
                        ref.dayOfWeek = datas.dayOfWeekById(body.dayId);
                    }
                    long lesN;
                    if(ref.dayOfWeek == null){
                        lesN = 0L;
                        ref.dayOfWeek = new DayOfWeek(Map.of(lesN, ref.lesson.getId()));
                        datas.getDayOfWeekRepository().saveAndFlush(ref.dayOfWeek);
                        ref.group.getDaysOfWeek().put(body.day, ref.dayOfWeek.getId());
                        datas.getGroupRepository().saveAndFlush(ref.group);
                    } else {
                        lesN = ref.dayOfWeek.getLessons().size();
                        ref.dayOfWeek.getLessons().put(lesN, ref.lesson.getId());
                        datas.getDayOfWeekRepository().saveAndFlush(ref.dayOfWeek);
                    }
                    body.wrtr.name("bodyT").beginObject();
                    datas.teachersBySchool(school, body.wrtr);
                    body.obj.addProperty("group", ref.group.getId());
                    body.wrtr.name("day").value(body.day)
                        .name("dayId").value(ref.dayOfWeek.getId())
                        .name("les").value(lesN);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            ans.add("body", body.obj);
            authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), "main", "ht", "main");
            authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), ref.group.getId()+"", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getSchedule")
    public JsonObject getSchedule(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                if(user.getSelRole() == 0L && user.getRoles().containsKey(0L)) {
                    ref.group = datas.groupById(user.getRoles().get(0L).getGroup());
                } else if(user.getSelRole() == 1L && user.getRoles().containsKey(1L)) {
                    User kidU = datas.userById(user.getSelKid());
                    Invite kidI = datas.inviteById(user.getSelKid());
                    if(kidU != null) {
                        ref.group = datas.groupById(kidU.getRoles().get(0L).getGroup());
                    }
                    if(kidI != null) {
                        ref.group = datas.groupById(kidI.getRole().get(0L).getGroup());
                    }
                } else if(user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                    ref.group = datas.groupById(body.group);
                }
                if (ref.group != null && !ObjectUtils.isEmpty(ref.group.getDaysOfWeek())) {
                    ref.schId = datas.getFirstRole(user.getRoles()).getYO();
                    body.wrtr.name("body").beginObject();
                    for (Map.Entry<Long, Long> entrD : ref.group.getDaysOfWeek().entrySet()) {
                        DayOfWeek dayOfWeek = datas.dayOfWeekById(entrD.getValue());
                        if (dayOfWeek == null || ObjectUtils.isEmpty(dayOfWeek.getLessons())) {
                            continue;
                        }
                        body.wrtr.name(entrD.getKey() + "").beginObject()
                            .name("dayId").value(entrD.getValue())
                            .name("lessons").beginObject();
                        for (Map.Entry<Long, Long> entrL : dayOfWeek.getLessons().entrySet()) {
                            Lesson lessonM = datas.lessonById(entrL.getValue());
                            if (lessonM == null) continue;
                            body.wrtr.name(entrL.getKey() + "").beginObject();
                            Subject subject = datas.subjectById(lessonM.getSubject());
                            if (subject != null) {
                                body.wrtr.name("name").value(subject.getName());
                            }
                            User teaU = datas.userById(lessonM.getTeacher());
                            Invite teaI = datas.inviteById(lessonM.getTeacherInv());
                            body.wrtr.name("cabinet").value(lessonM.getKab())
                                .name("prepod").beginObject();
                            if (teaU != null) {
                                body.wrtr.name("name").value(teaU.getFio())
                                    .name("id").value(teaU.getId());
                            } else if (teaI != null) {
                                body.wrtr.name("name").value(teaI.getFio())
                                    .name("id").value(teaI.getId());
                            }
                            body.wrtr.endObject().name("group").value(ref.group.getName())
                                .endObject();
                        }
                        body.wrtr.endObject().endObject();
                    }
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), subscriber.getType(), ref.schId +"", user.getRoles().containsKey(3L) ? "main" : ref.group.getId()+"", subscriber.getLvlMore1(), "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null, firstG;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                if(user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L)) {
                    body.wrtr.name("bodyG").beginObject();
                    ref.firstG = datas.groupsByUser(user, body.wrtr);
                    ref.schId = datas.getFirstRole(user.getRoles()).getYO();
                    School school = datas.schoolById(ref.schId);
                    body.wrtr.name("firstG").value(ref.firstG)
                        .name("bodyT").beginObject();
                    datas.teachersBySchool(school, body.wrtr);
                }
                if(user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L)) {
                    body.wrtr.name("role").value(true);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.SCHEDULE, ref.schId +"", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataSchedule {
    public String uuid;
    public JsonObject obj;
    public Long group, dayId, day;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}