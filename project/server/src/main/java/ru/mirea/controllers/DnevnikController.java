package ru.mirea.controllers;

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
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.services.ServerService;

import java.util.Comparator;
import java.util.List;

@RequestMapping("/dnevnik")
@NoArgsConstructor
@RestController public class DnevnikController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/getDnevnik")
    public JsonObject getDnevnik(@RequestBody DataDnevnik body) {
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
                    ref.group = user.getRoles().get(0L).getGrp();
                } else if(user.getSelRole() == 1L && user.getRoles().containsKey(1L)) {
                    User kidU = datas.userById(user.getSelKid());
                    Invite kidI = datas.inviteById(user.getSelKid());
                    if(kidU != null) {
                        ref.group = kidU.getRoles().get(0L).getGrp();
                    } else if(kidI != null) {
                        ref.group = kidI.getRoles().get(0L).getGrp();
                    }
                }
                if (ref.group != null || user.getSelRole() == 2L) {
                    ref.schId = datas.getFirstRole(user.getRoles()).getYO().getId();
                    List<Lesson> lessons;
                    if(user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
                        lessons = datas.getLessonRepository().findBySchoolAndTeacher(ref.schId, user.getId());
                    } else {
                        lessons = datas.getLessonRepository().findBySchoolAndGrp(ref.schId, ref.group.getId());
                    }
                    body.wrtr.name("body").beginObject();
                    lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
                    int k1 = -1;
                    for(Lesson les : lessons) {
                        if(k1 != les.getDayWeek()) {
                            if(k1 != -1) body.wrtr.endObject().endObject();
                            k1 = les.getDayWeek();
                            body.wrtr.name(k1 + "").beginObject()
                                .name("lessons").beginObject();
                        }
                        body.wrtr.name(les.getNumLesson() + "").beginObject();
                        if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                            body.wrtr.name("name").value(les.getNameSubject());
                        }
                        body.wrtr.name("cabinet").value(les.getKab());
                        if(user.getSelRole() == 2L) {
                            Group grp = les.getGrp();
                            body.wrtr.name("group").value(grp.getName());
                        } else {
                            User teaU = les.getTeacher();
                            Invite teaI = les.getTeacherInv();
                            body.wrtr.name("prepod").beginObject();
                            if (teaU != null) {
                                body.wrtr.name("name").value(teaU.getFio())
                                    .name("id").value(teaU.getId());
                            } else if (teaI != null) {
                                body.wrtr.name("name").value(teaI.getFio())
                                    .name("id").value(teaI.getId());
                            }
                            body.wrtr.endObject();
                        }
                        body.wrtr.endObject();
                    }
                    if(k1 != -1) body.wrtr.endObject().endObject();
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            boolean b = user.getSelRole() == 2L || user.getSelRole() == 3L;
            authController.infCon(body.uuid, null, null, null, b ? null : ref.group.getId()+"", null, null);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataDnevnik body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                ref.schId = datas.getFirstRole(user.getRoles()).getYO().getId();
                if(user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L)) {
                    body.wrtr.name("yes").value(true);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.DNEVNIK, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataDnevnik {
    public String uuid;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}