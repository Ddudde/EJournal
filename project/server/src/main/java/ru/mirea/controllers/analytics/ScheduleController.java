package ru.mirea.controllers.analytics;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.*;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static java.util.Arrays.asList;

@RestController
@RequestMapping("/schedule")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
public class ScheduleController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping
    public JsonObject post(@RequestBody JsonObject data) {
        System.out.println("Post! " + data);
        JsonObject ans = new JsonObject(), body = null, bodyAns;
        ans.addProperty("error", false);
        if(data.has("body") && data.get("body").isJsonObject()) body = data.get("body").getAsJsonObject();
        if(!data.has("type")) data.addProperty("type", "default");
        switch (data.get("type").getAsString()){
            case "getInfo" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                JsonObject bodyG = new JsonObject(),
                        bodyT = new JsonObject();
                Long schId = null;
                if(user != null && (user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L))) {
                    ans.addProperty("firstG", datas.groupsByUser(user, bodyG));
                    ans.add("bodyG", bodyG);
                    schId = datas.getFirstRole(user.getRoles()).getYO();
                    School school = datas.schoolById(schId);
                    datas.teachersBySchool(school, bodyT);
                    ans.add("bodyT", bodyT);

                }
                if(ObjectUtils.isEmpty(bodyG.keySet()) || ObjectUtils.isEmpty(bodyT.keySet())){
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "getSchedule" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = datas.groupById(body.get("group").getAsLong());
                JsonObject prepod = null;
                Long schId = null;
                if(user != null && group != null && !ObjectUtils.isEmpty(group.getDaysOfWeek())){
                    schId = datas.getFirstRole(user.getRoles()).getYO();
                    bodyAns = new JsonObject();
                    ans.add("body", bodyAns);
                    for(Long i : group.getDaysOfWeek()) {
                        DayOfWeek dayOfWeek = datas.dayOfWeekById(i);
                        if(dayOfWeek == null || ObjectUtils.isEmpty(dayOfWeek.getLessons())) continue;
                        JsonObject dayO = new JsonObject(),
                        lessonO;
                        bodyAns.add(i+"", dayO);
                        for(Long i1 : dayOfWeek.getLessons()){
                            Lesson lessonM = datas.lessonById(i1);
                            if(lessonM == null) continue;
                            lessonO = new JsonObject();
                            dayO.add(i1+"", lessonO);
                            Subject subject = datas.subjectById(lessonM.getSubject());
                            if(subject != null) {
                                lessonO.addProperty("name", subject.getName());
                            }
                            lessonO.addProperty("cabinet", lessonM.getKab());
                            User teaU = datas.userById(lessonM.getTeacher());
                            Invite teaI = datas.inviteById(lessonM.getTeacherInv());
                            prepod = new JsonObject();
                            lessonO.add("prepod", prepod);
                            if(teaU != null) {
                                prepod.addProperty("name", teaU.getFio());
                                prepod.addProperty("id", teaU.getId());
                            } else if(teaI != null) {
                                prepod.addProperty("name", teaI.getFio());
                                prepod.addProperty("id", teaI.getId());
                            }
                            lessonO.addProperty("group", group.getName());
                        }
                    }
                }
                if(prepod == null){
                    ans.addProperty("error", true);
                } else {
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.SCHEDULE, schId+"", user.getRoles().containsKey(3L) ? "main" : group.getId()+"", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
                }
                return ans;
            }
            case "addLesson" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                DayOfWeek dayOfWeek = null;
                JsonObject obj = null;
                Group group = null;
                Lesson lesson = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    group = datas.groupById(body.get("group").getAsLong());
                    if(group != null) {
                        if(group.getDaysOfWeek() == null) {
                            group.setDaysOfWeek(new ArrayList<>(asList()));
                        }
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
                            if(ObjectUtils.isEmpty(teaU.getRoles().get(2L).getSubjects())){
                                teaU.getRoles().get(2L).setSubjects(new ArrayList<>(asList()));
                            }
                            teaU.getRoles().get(2L).getSubjects().add(subject.getId());
                            datas.getUserRepository().saveAndFlush(teaU);
                            if(ObjectUtils.isEmpty(subject.getTeachers())){
                                subject.setTeachers(new ArrayList<>(asList()));
                            }
                            subject.getTeachers().add(teaU.getId());
                            datas.getSubjectRepository().saveAndFlush(subject);

                        } else if(teaI != null){
                            lesson.setTeacherInv(teaId);
                            if(!ObjectUtils.isEmpty(school.getTeachersInv())
                                    && school.getTeachersInv().contains(teaId)){
                                school.getTeachersInv().remove(teaId);
                            }
                            if(ObjectUtils.isEmpty(teaI.getRole().get(2L).getSubjects())){
                                teaI.getRole().get(2L).setSubjects(new ArrayList<>(asList()));
                            }
                            teaI.getRole().get(2L).getSubjects().add(subject.getId());
                            datas.getInviteRepository().saveAndFlush(teaI);
                            if(ObjectUtils.isEmpty(subject.getTeachersInv())){
                                subject.setTeachersInv(new ArrayList<>(asList()));
                            }
                            subject.getTeachersInv().add(teaI.getId());
                            datas.getSubjectRepository().saveAndFlush(subject);
                        }
                        datas.getLessonRepository().saveAndFlush(lesson);
                        dayOfWeek = datas.dayOfWeekById(body.get("day").getAsLong());
                        if(dayOfWeek == null){
                            dayOfWeek = new DayOfWeek(new ArrayList<>(asList(lesson.getId())));
                            datas.getDayOfWeekRepository().saveAndFlush(dayOfWeek);
                            group.getDaysOfWeek().add(dayOfWeek.getId());
                            datas.getGroupRepository().saveAndFlush(group);
                        } else {
                            dayOfWeek.getLessons().add(lesson.getId());
                            datas.getDayOfWeekRepository().saveAndFlush(dayOfWeek);
                        }
                    }
                }
                if(dayOfWeek == null){
                    ans.addProperty("error", true);
                } else {
                    ans.add("body", obj);
                    ans.add("day", body.get("day"));
                    ans.addProperty("id", lesson.getId());
                    obj.addProperty("group", group.getId());

                    authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), "main", "ht", "main");
                    authController.sendMessageForAll("addLessonC", ans, TypesConnect.SCHEDULE, subscriber.getLvlSch(), group.getId()+"", "main", "main");
                }
                return ans;
            }
            case "setCodePep" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userById(body.get("id1").getAsLong());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                Long schId = null;
                if(user != null && (user1 != null || inv != null)
                        && body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L)) {
                    UUID uuid = UUID.randomUUID();
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    if(user1 != null){
                        user1.setCode(uuid.toString());
                        user1.setExpDate(Main.df.format(dateAfter));
                        datas.getUserRepository().saveAndFlush(user1);
                        schId = datas.getFirstRole(user1.getRoles()).getYO();

                        ans.addProperty("id1", user1.getId());
                    } else if(inv != null){
                        inv.setCode(uuid.toString());
                        inv.setExpDate(Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);
                        schId = datas.getFirstRole(inv.getRole()).getYO();

                        ans.addProperty("id1", inv.getId());
                    }
                    System.out.println("setCode " + uuid);

                    ans.addProperty("code", uuid.toString());
                    ans.add("id", body.get("id"));
                    authController.sendMessageForAll("codPepL1C", ans, subscriber.getType(), schId+"", "main", "ht", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chPep1" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                if(user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                    if(user1 != null){
                        user1.setFio(body.get("name").getAsString());
                        datas.getUserRepository().saveAndFlush(user1);

                        ans.addProperty("id", user1.getId());
                    } else if(inv != null){
                        inv.setFio(body.get("name").getAsString());
                        datas.getInviteRepository().saveAndFlush(inv);

                        ans.addProperty("id", inv.getId());
                    }

                    ans.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("chPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "remPep" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                if(user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                    Group group = datas.groupById(Long.parseLong(subscriber.getLvlGr()));
                    if(group != null) {
                        if (user1 != null) {
                            user1.getRoles().remove(3L);
                            datas.getUserRepository().saveAndFlush(user1);
                            if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1.getId());
                            datas.getGroupRepository().saveAndFlush(group);

                            ans.addProperty("id", user1.getId());
                        } else if (inv != null) {
                            datas.getInviteRepository().delete(inv);
                            if (!ObjectUtils.isEmpty(group.getKidsInv())) group.getKidsInv().remove(inv.getId());
                            datas.getGroupRepository().saveAndFlush(group);

                            ans.addProperty("id", inv.getId());
                        }
                    }
                }
                if(ans.has("id")){
                    authController.sendMessageForAll("remPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            default -> {
                System.out.println("Error Type" + data.get("type"));
                ans.addProperty("error", true);
                return ans;
            }
        }
    }
}