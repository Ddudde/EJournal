package ru.mirea.controllers.analytics;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.controllers.AuthController;
import ru.mirea.controllers.CallInterface;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.dayOfWeek.DayOfWeek;
import ru.mirea.data.models.school.dayOfWeek.Lesson;
import ru.mirea.data.models.school.dayOfWeek.Subject;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

@RestController
@RequestMapping("/schedule")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000", "https://ddudde.github.io"})
public class ScheduleController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addLesson")
    public JsonObject addLesson(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        JsonObject obj = null;
        Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
        User user = datas.userByLogin(subscriber.getLogin());
        DayOfWeek dayOfWeek = null;
        Group group = null;
        Lesson lesson = null;
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && user.getRoles().containsKey(3L)) {
                group = datas.groupById(body.get("group").getAsLong());
                if(group != null) {
                    lesson = new Lesson();
                    obj = body.getAsJsonObject("obj");
                    Long schId = Long.parseLong(subscriber.getLvlSch()),
                            teaId = obj.getAsJsonObject("prepod").get("id").getAsLong();
                    School school = datas.schoolById(schId);
                    Subject subject = datas.subjectByNameAndSchool(obj.get("name").getAsString(), schId);
                    if(subject == null) {
                        subject = new Subject(obj.get("name").getAsString(), schId, new ArrayList<>(asList(teaId)));
                        datas.getSubjectRepository().saveAndFlush(subject);
                    }
                    lesson.setSubject(subject.getId());
                    lesson.setKab(obj.get("cabinet").getAsString());
                    User teaU = datas.userById(teaId);
                    Invite teaI = datas.inviteById(teaId);
                    if(teaU != null) {
                        lesson.setTeacher(teaId);
                        if(!ObjectUtils.isEmpty(school.getTeachers())
                                && school.getTeachers().contains(teaId)){
                            school.getTeachers().remove(teaId);
                        }
                        teaU.getRoles().get(2L).getSubjects().add(subject.getId());
                        datas.getUserRepository().saveAndFlush(teaU);
                        subject.getTeachers().add(teaU.getId());
                        datas.getSubjectRepository().saveAndFlush(subject);

                    } else if(teaI != null){
                        lesson.setTeacherInv(teaId);
                        if(!ObjectUtils.isEmpty(school.getTeachersInv())
                                && school.getTeachersInv().contains(teaId)){
                            school.getTeachersInv().remove(teaId);
                        }
                        teaI.getRole().get(2L).getSubjects().add(subject.getId());
                        datas.getInviteRepository().saveAndFlush(teaI);
                        subject.getTeachersInv().add(teaI.getId());
                        datas.getSubjectRepository().saveAndFlush(subject);
                    }
                    datas.getLessonRepository().saveAndFlush(lesson);
                    if(body.has("dayId")) {
                        dayOfWeek = datas.dayOfWeekById(body.get("dayId").getAsLong());
                    }
                    long lesN;
                    if(dayOfWeek == null){
                        Lesson finalLesson = lesson;
                        lesN = 0L;
                        dayOfWeek = new DayOfWeek(new HashMap<Long,Long>() {{
                            put(lesN, finalLesson.getId());
                        }});
                        datas.getDayOfWeekRepository().saveAndFlush(dayOfWeek);
                        group.getDaysOfWeek().put(body.get("day").getAsLong(), dayOfWeek.getId());
                        datas.getGroupRepository().saveAndFlush(group);
                    } else {
                        lesN = dayOfWeek.getLessons().size();
                        dayOfWeek.getLessons().put(lesN, lesson.getId());
                        datas.getDayOfWeekRepository().saveAndFlush(dayOfWeek);
                    }
                    obj.addProperty("group", group.getId());
                    wrtr.name("day").value(body.get("day").toString())
                        .name("dayId").value(dayOfWeek.getId())
                        .name("les").value(lesN);
                }
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        JsonObject finalObj = obj;
        Group finalGroup = group;
        return datas.getObj(ans -> {
            ans.add("body", finalObj);
            authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), "main", "ht", "main");
            authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), finalGroup.getId()+"", "main", "main");
        }, wrtr, true);
    }

    @PostMapping(value = "/getSchedule")
    public JsonObject getSchedule(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
        User user = datas.userByLogin(subscriber.getLogin());
        Group group = datas.groupById(body.get("group").getAsLong());
        Long schId = null;
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && group != null && !ObjectUtils.isEmpty(group.getDaysOfWeek())){
                schId = datas.getFirstRole(user.getRoles()).getYO();
                wrtr.name("body").beginObject();
                for(Map.Entry<Long, Long> entrD : group.getDaysOfWeek().entrySet()) {
                    DayOfWeek dayOfWeek = datas.dayOfWeekById(entrD.getValue());
                    if(dayOfWeek == null || ObjectUtils.isEmpty(dayOfWeek.getLessons())) {
                        continue;
                    }
                    wrtr.name(entrD.getKey()+"").beginObject()
                        .name("dayId").value(entrD.getValue())
                        .name("lessons").beginObject();
                    for(Map.Entry<Long, Long> entrL : dayOfWeek.getLessons().entrySet()) {
                        Lesson lessonM = datas.lessonById(entrL.getValue());
                        if(lessonM == null) continue;
                        wrtr.name(entrL.getKey()+"").beginObject();
                        Subject subject = datas.subjectById(lessonM.getSubject());
                        if(subject != null) {
                            wrtr.name("name").value(subject.getName());
                        }
                        User teaU = datas.userById(lessonM.getTeacher());
                        Invite teaI = datas.inviteById(lessonM.getTeacherInv());
                        wrtr.name("cabinet").value(lessonM.getKab())
                            .name("prepod").beginObject();
                        if(teaU != null) {
                            wrtr.name("name").value(teaU.getFio())
                                .name("id").value(teaU.getId());
                        } else if(teaI != null) {
                            wrtr.name("name").value(teaI.getFio())
                                .name("id").value(teaI.getId());
                        }
                        wrtr.endObject().name("group").value(group.getName())
                            .endObject();
                    }
                    wrtr.endObject().endObject();
                }
                wrtr.endObject();
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        Long finalSchId = schId;
        return datas.getObj(ans -> {
            authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), subscriber.getType(), finalSchId +"", user.getRoles().containsKey(3L) ? "main" : group.getId()+"", subscriber.getLvlMore1(), "main");
        }, wrtr, true);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
        User user = datas.userByLogin(subscriber.getLogin());
        Long schId = null, firstG;
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && (user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L))) {
                wrtr.name("bodyG").beginObject();
                firstG = datas.groupsByUser(user, wrtr);
                wrtr.name("firstG").value(firstG);
                schId = datas.getFirstRole(user.getRoles()).getYO();
                School school = datas.schoolById(schId);
                wrtr.name("bodyT").beginObject();
                datas.teachersBySchool(school, wrtr);
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        Long finalSchId = schId;
        return datas.getObj(ans -> {
            authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.SCHEDULE, finalSchId +"", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, wrtr, true);
    }
}