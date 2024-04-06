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
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.data.models.school.School;
import ru.mirea.services.MainService;

import java.util.Comparator;
import java.util.List;

@RequestMapping("/schedule")
@NoArgsConstructor
@RestController public class ScheduleController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addLesson")
    public JsonObject addLesson(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            User teaU = null;
            Group group = null;
            Lesson lesson = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                ref.group = datas.getDbService().groupById(body.group);
                if(ref.group != null) {
                    ref.lesson = new Lesson();
                    Long schId = Long.parseLong(subscriber.getLvlSch()),
                        teaId = body.obj.getAsJsonObject("prepod").get("id").getAsLong();
                    School school = datas.getDbService().schoolById(schId);
                    ref.lesson.setNameSubject(body.obj.get("name").getAsString());
                    ref.lesson.setKab(body.obj.get("cabinet").getAsString());
                    ref.teaU = datas.getDbService().userById(teaId);
                    if(ref.teaU != null) {
                        ref.lesson.setTeacher(ref.teaU);
                        if(!ObjectUtils.isEmpty(school.getTeachers())
                            && school.getTeachers().contains(ref.teaU)){
                            school.getTeachers().remove(ref.teaU);
                        }
                        if(!ref.teaU.getRoles().get(2L).getSubjects().contains(ref.lesson.getNameSubject())) {
                            ref.teaU.getRoles().get(2L).getSubjects().add(ref.lesson.getNameSubject());
                            datas.getDbService().getUserRepository().saveAndFlush(ref.teaU);
                        }
                    }
                    ref.lesson.setGrp(datas.getDbService().groupById(ref.group.getId()));
                    ref.lesson.setSchool(school);
                    ref.lesson.setDayWeek(body.day);
                    List<Lesson> lessons = datas.getDbService().getLessonRepository().findBySchoolIdAndGrpIdAndDayWeek(school.getId(), ref.group.getId(), body.day);
                    lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
                    if(lessons.isEmpty()) {
                        ref.lesson.setNumLesson(0);
                    } else {
                        ref.lesson.setNumLesson(lessons.get(lessons.size()-1).getNumLesson()+1);
                    }
                    datas.getDbService().getLessonRepository().saveAndFlush(ref.lesson);
                    datas.getDbService().getSchoolRepository().saveAndFlush(school);
                    body.wrtr.name("bodyT").beginObject();
                    datas.teachersBySchool(school, body.wrtr);
                    body.obj.addProperty("group", ref.group.getName());
                    body.wrtr.name("day").value(body.day)
                        .name("les").value(ref.lesson.getNumLesson());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            ans.add("body", body.obj);
            if(ref.teaU != null) {
                authController.sendMessageFor("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), "main", "tea", ref.teaU.getId()+"");
            }
            authController.sendMessageFor("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), "main", "ht", "main");
            authController.sendMessageFor("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), ref.group.getId()+"", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getSchedule")
    public JsonObject getSchedule(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                if(user.getSelRole() == 0L && user.getRoles().containsKey(0L)) {
                    ref.group = user.getRoles().get(0L).getGrp();
                } else if(user.getSelRole() == 1L && user.getRoles().containsKey(1L)) {
                    User kidU = datas.getDbService().userById(user.getSelKid());
                    if(kidU != null) {
                        ref.group = kidU.getRoles().get(0L).getGrp();
                    }
                } else if(user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                    ref.group = datas.getDbService().groupById(body.group);
                }
                datas.getShedule("body", user, body.wrtr, ref.group != null ? ref.group.getId() : null);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            boolean b = user.getSelRole() == 2L || user.getSelRole() == 3L;
            authController.infCon(body.uuid, null, null, null, b ? null : ref.group.getId()+"", null, null);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataSchedule body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null, firstG;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                ref.schId = datas.getDbService().getFirstRole(user.getRoles()).getYO().getId();
                if(user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L)) {
                    body.wrtr.name("bodyG").beginObject();
                    ref.firstG = datas.groupsBySchoolOfUser(user, body.wrtr);
                    School school = datas.getDbService().schoolById(ref.schId);
                    body.wrtr.name("firstG").value(ref.firstG)
                        .name("bodyT").beginObject();
                    datas.teachersBySchool(school, body.wrtr);
                }
                if(user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L)) {
                    body.wrtr.name("yes").value(true);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            String l1 = "main", l2 = "main";
            if(user.getSelRole() == 3L) l1 = "ht";
            if(user.getSelRole() == 2L) {
                l1 = "tea";
                l2 = user.getId()+"";
            }
            authController.infCon(body.uuid, null, TypesConnect.SCHEDULE, ref.schId +"", "main", l1, l2);
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataSchedule {
    public String uuid;
    public JsonObject obj;
    public Long group;
    public int day;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}