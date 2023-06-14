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
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RequestMapping("/students")
@NoArgsConstructor
@RestController public class StudentsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                Group group = datas.groupById(Long.parseLong(subscriber.getLvlGr()));
                if (group != null) {
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
            authController.sendMessageForAll("remPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L) && (user1 != null || inv != null)) {
                if (user1 != null) {
                    user1.setFio(body.name);
                    datas.getUserRepository().saveAndFlush(user1);

                    body.wrtr.name("id").value(user1.getId());
                } else if (inv != null) {
                    inv.setFio(body.name);
                    datas.getInviteRepository().saveAndFlush(inv);

                    body.wrtr.name("id").value(inv.getId());
                }
                body.wrtr.name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addPep")
    public JsonObject addPep(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                Group group = datas.groupById(Long.parseLong(subscriber.getLvlGr()));
                if (group != null) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    Role role = new Role(null, datas.schoolById(Long.parseLong(subscriber.getLvlSch())), group);
                    datas.getRoleRepository().saveAndFlush(role);
                    Invite inv = new Invite(body.name, Map.of(
                        0L, role
                    ), Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    group.getKidsInv().add(inv);
                    datas.getGroupRepository().saveAndFlush(group);

                    body.wrtr.name("id").value(inv.getId())
                        .name("body").beginObject()
                        .name("name").value(body.name).endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getStud")
    public JsonObject getStud(@RequestBody DataStudents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null, grId = body.group;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null) {
                School school = datas.getFirstRole(user.getRoles()).getYO();
                ref.schId = school.getId();
                if (!user.getRoles().containsKey(3L)) {
                    ref.grId = datas.getFirstRole(user.getRoles()).getGrp().getId();
                }
                Group group = datas.groupById(ref.grId);
                if (group != null && school != null && school.getGroups().contains(group.getId())) {
                    body.wrtr.name("body").beginObject();
                    datas.usersByList(group.getKids(), true, body.wrtr);
                    datas.invitesByList(group.getKidsInv(), true, body.wrtr);
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
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null) {
                if(user.getSelRole() == 3L) {
                    body.wrtr.name("bodyG").beginObject();
                    Long firstG = datas.groupsByUser(user, body.wrtr);
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
    public String uuid, name, id;
    public Long group, id1;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}