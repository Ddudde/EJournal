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
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.MainService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/parents")
@NoArgsConstructor
@RestController public class ParentsController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            Group group = datas.getDbService().groupById(Long.parseLong(subscriber.getLvlGr()));
            if (user != null && user.getRoles().containsKey(3L) && group != null && user1 != null) {
                user1.getRoles().remove(3L);
                datas.getDbService().getUserRepository().saveAndFlush(user1);
                if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1.getId());
                datas.getDbService().getGroupRepository().saveAndFlush(group);

                body.wrtr.name("id").value(user1.getId());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("remPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataParents body) {
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
            authController.sendMessageFor("chPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user1 != null
                    && user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                user1.setCode(uuid.toString());
                user1.setExpDate(Main.df.format(dateAfter));
                datas.getDbService().getUserRepository().saveAndFlush(user1);
                ref.schId = datas.getDbService().getFirstRole(user1.getRoles()).getYO().getId();

                body.wrtr.name("id1").value(user1.getId());
                System.out.println("setCode " + uuid);

                body.wrtr.name("code").value(uuid.toString())
                    .name("id").value(body.id);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("codPepL1C", ans, subscriber.getType(), ref.schId + "", subscriber.getLvlGr(), "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addKid")
    public JsonObject addKid(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                User kidU = datas.getDbService().userById(body.id);
                JsonObject par = body.bod.getAsJsonObject("par");
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if (kidU != null) {
                    body.wrtr.name("id").value(kidU.getId())
                        .name("body").beginObject()
                        .name("name").value(kidU.getFio())
                        .name("login").value(kidU.getUsername())
                        .name("par").beginObject();
                    for (String id : par.keySet()) {
                        Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null, datas.getDbService().schoolById(Long.parseLong(subscriber.getLvlSch()))));
                        User inv = new User(par.getAsJsonObject(id).get("name").getAsString(), Map.of(
                            1L, role
                        ), Main.df.format(dateAfter));
                        datas.getDbService().getUserRepository().saveAndFlush(inv);

                        body.wrtr.name(inv.getId() + "").beginObject()
                            .name("name").value(inv.getFio());

                        if (!inv.getRoles().get(1L).getKids().contains(kidU)) {
                            inv.getRoles().get(1L).getKids().add(kidU);
                        }
                        kidU.getRoles().get(0L).getParents().add(inv);
                        datas.getDbService().getUserRepository().saveAndFlush(kidU);

                        body.wrtr.endObject();
                    }
                    body.wrtr.endObject().endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("addKidC", ans, TypesConnect.PARENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }


    @PostMapping(value = "/getParents")
    public JsonObject getParents(@RequestBody DataParents body) {
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
                    body.wrtr.name("bodyP").beginObject();
                    if (!ObjectUtils.isEmpty(group.getKids())) {
                        for (User studU : group.getKids()) {
                            if (studU == null) continue;
                            body.wrtr.name(studU.getId() + "").beginObject()
                                .name("name").value(studU.getFio());
                            if (!ObjectUtils.isEmpty(studU.getUsername())) {
                                body.wrtr.name("login").value(studU.getUsername());
                            }
                            if (!ObjectUtils.isEmpty(studU.getCode())) {
                                body.wrtr.name("link").value(studU.getCode());
                            }
                            body.wrtr.name("par").beginObject();
                            datas.usersByList(studU.getRoles().get(0L).getParents(), true, body.wrtr);
                            body.wrtr.endObject().endObject();
                        }
                    }
                    body.wrtr.endObject().name("bodyC").beginObject();
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
    public JsonObject getInfo(@RequestBody DataParents body) {
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
            authController.infCon(body.uuid, null, TypesConnect.PARENTS, "main", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataParents {
    public String uuid, name;
    public Long group, id;
    public JsonObject bod;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}