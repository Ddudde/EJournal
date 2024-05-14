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
import java.util.Set;
import java.util.UUID;

@RequestMapping("/teachers")
@NoArgsConstructor
@RestController public class TeachersController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L) && user1 != null) {
                Group group = datas.getDbService().groupById(Long.parseLong(subscriber.getLvlGr()));
                if(group != null) {
                    user1.getRoles().remove(3L);
                    datas.getDbService().getUserRepository().saveAndFlush(user1);
                    if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1.getId());
                    datas.getDbService().getGroupRepository().saveAndFlush(group);

                    body.wrtr.name("id").value(user1.getId());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("remPepC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L) && user1 != null) {
                user1.setFio(body.name);
                datas.getDbService().getUserRepository().saveAndFlush(user1);

                body.wrtr.name("id").value(user1.getId())
                    .name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("chPepC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user1 != null
                && user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                user1.setCode(uuid.toString());
                user1.setExpDate(Main.df.format(dateAfter));
                datas.getDbService().getUserRepository().saveAndFlush(user1);
                ref.schId = datas.getDbService().getFirstRole(user1.getRoles()).getYO().getId();

                System.out.println("setCode " + uuid);
                body.wrtr.name("id1").value(user1.getId())
                    .name("code").value(uuid.toString())
                    .name("id").value(body.id);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("codPepL1C", ans, subscriber.getType(), ref.schId+"", "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addTea")
    public JsonObject addTea(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                School school = datas.getDbService().schoolById(Long.parseLong(subscriber.getLvlSch()));
                if(school != null) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null, Set.of(), school));
                    User inv = new User(body.name, Map.of(
                        2L, role
                    ), Main.df.format(dateAfter));
                    datas.getDbService().getUserRepository().saveAndFlush(inv);
                    school.getTeachers().add(inv);
                    datas.getDbService().getSchoolRepository().saveAndFlush(school);

                    body.wrtr.name("id").value(inv.getId());
                    body.wrtr.name("name").value(body.name);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("addTeaC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getTeachers")
    public JsonObject getTeachers(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                ref.schId = school.getId();
                if(school != null) {
                    body.wrtr.name("body").beginObject();
                    datas.teachersBySchool(school, body.wrtr);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.TEACHERS, ref.schId+"", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataTeachers {
    public String uuid, name;
    public Long id;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}