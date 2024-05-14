package ru.controllers.people;

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
import ru.Main;
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.Role;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.services.MainService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RequestMapping("/students")
@NoArgsConstructor
@RestController public class StudentsController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(3L) && user1 != null) {
                Group group = datas.getDbService().groupById(Long.parseLong(subscriber.getLvlGr()));
                if (group != null) {
                    user1.getRoles().remove(3L);
                    datas.getDbService().getUserRepository().saveAndFlush(user1);
                    if (!ObjectUtils.isEmpty(group.getKids())) {
                        group.getKids().remove(user1);
                    }
                    datas.getDbService().getGroupRepository().saveAndFlush(group);

                    body.wrtr.name("id").value(user1.getId());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("remPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(3L) && user1 != null) {
                user1.setFio(body.name);
                datas.getDbService().getUserRepository().saveAndFlush(user1);

                body.wrtr.name("id").value(user1.getId())
                    .name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("chPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addPep")
    public JsonObject addPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                Group group = datas.getDbService().groupById(Long.parseLong(subscriber.getLvlGr()));
                if (group != null) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null, datas.getDbService().schoolById(Long.parseLong(subscriber.getLvlSch())), group));
                    User inv = new User(body.name, Map.of(
                        0L, role
                    ), Main.df.format(dateAfter));
                    datas.getDbService().getUserRepository().saveAndFlush(inv);
                    group.getKids().add(inv);
                    datas.getDbService().getGroupRepository().saveAndFlush(group);

                    body.wrtr.name("id").value(inv.getId())
                        .name("body").beginObject()
                        .name("name").value(body.name).endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("addPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getStud")
    public JsonObject getStud(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null, grId = body.group;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null) {
                School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                ref.schId = school.getId();
                if (!user.getRoles().containsKey(3L)) {
                    ref.grId = datas.getDbService().getFirstRole(user.getRoles()).getGrp().getId();
                }
                Group group = datas.getDbService().groupById(ref.grId);
                if (group != null && school != null && school.getGroups().contains(group.getId())) {
                    body.wrtr.name("body").beginObject();
                    datas.usersByList(group.getKids(), true, body.wrtr);
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, null, ref.schId + "", ref.grId + "", null, null);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null) {
                if(user.getSelRole() == 3L) {
                    body.wrtr.name("bodyG").beginObject();
                    Long firstG = datas.groupsBySchoolOfUser(user, body.wrtr);
                    body.wrtr.name("firstG").value(firstG);
                } else {
                    body.wrtr.name("yes").value(true);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.STUDENTS, "main", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataStudents {
    public String uuid, name;
    public Long group, id;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}