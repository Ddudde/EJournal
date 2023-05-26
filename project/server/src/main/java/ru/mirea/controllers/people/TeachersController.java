package ru.mirea.controllers.people;

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
import ru.mirea.data.json.Role;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/teachers")
@NoArgsConstructor
@RestController public class TeachersController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                Group group = datas.groupById(Long.parseLong(subscriber.getLvlGr()));
                if(group != null) {
                    if (user1 != null) {
                        user1.getRoles().remove(3L);
                        datas.getUserRepository().saveAndFlush(user1);
                        if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1.getId());
                        datas.getGroupRepository().saveAndFlush(group);

                        body.wrtr.name("id").value(user1.getId());
                    } else if (inv != null) {
                        datas.getInviteRepository().delete(inv);
                        if (!ObjectUtils.isEmpty(group.getKidsInv())) group.getKidsInv().remove(inv.getId());
                        datas.getGroupRepository().saveAndFlush(group);

                        body.wrtr.name("id").value(inv.getId());
                    }
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("remPepC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                if(user1 != null){
                    user1.setFio(body.name);
                    datas.getUserRepository().saveAndFlush(user1);

                    body.wrtr.name("id").value(user1.getId());
                } else if(inv != null){
                    inv.setFio(body.name);
                    datas.getInviteRepository().saveAndFlush(inv);

                    body.wrtr.name("id").value(inv.getId());
                }
                body.wrtr.name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chPepC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userById(body.id1);
        Invite inv = datas.inviteById(body.id1);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && (user1 != null || inv != null)
                && user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if(user1 != null){
                    user1.setCode(uuid.toString());
                    user1.setExpDate(Main.df.format(dateAfter));
                    datas.getUserRepository().saveAndFlush(user1);
                    ref.schId = datas.getFirstRole(user1.getRoles()).getYO();

                    body.wrtr.name("id1").value(user1.getId());
                } else if(inv != null){
                    inv.setCode(uuid.toString());
                    inv.setExpDate(Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    ref.schId = datas.getFirstRole(inv.getRoles()).getYO();

                    body.wrtr.name("id1").value(inv.getId());
                }
                System.out.println("setCode " + uuid);
                body.wrtr.name("code").value(uuid.toString());
                body.wrtr.name("id").value(body.id);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("codPepL1C", ans, subscriber.getType(), ref.schId+"", "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addTea")
    public JsonObject addTea(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                School school = datas.schoolById(Long.parseLong(subscriber.getLvlSch()));
                if(school != null) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    Invite inv = new Invite(body.name, Map.of(
                        2L, new Role(null, null, Long.parseLong(subscriber.getLvlSch()))
                    ), Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    school.getTeachersInv().add(inv.getId());
                    datas.getSchoolRepository().saveAndFlush(school);

                    body.wrtr.name("id").value(inv.getId());
                    body.wrtr.name("name").value(body.name);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addTeaC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getTeachers")
    public JsonObject getTeachers(@RequestBody DataTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                ref.schId = datas.getFirstRole(user.getRoles()).getYO();
                School school = datas.schoolById(ref.schId);
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
    public String uuid, name, id;
    public Long id1;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}