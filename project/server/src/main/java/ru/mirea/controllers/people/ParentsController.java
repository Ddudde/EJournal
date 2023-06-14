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
import java.util.UUID;

@RequestMapping("/parents")
@NoArgsConstructor
@RestController public class ParentsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.login);//id
        Invite inv = datas.inviteById(body.id);//id1
        try {
            body.wrtr = datas.ini(body.toString());
            Group group = datas.groupById(Long.parseLong(subscriber.getLvlGr()));
            if (user != null && user.getRoles().containsKey(3L) && group != null) {
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
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("remPepC", ans, TypesConnect.STUDENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.login);//id
        Invite inv = datas.inviteById(body.id);//id1
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

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userById(body.id1);
        Invite inv = datas.inviteById(body.id1);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && (user1 != null || inv != null)
                    && user.getSelRole() == 3L && user.getRoles().containsKey(3L)) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if (user1 != null) {
                    user1.setCode(uuid.toString());
                    user1.setExpDate(Main.df.format(dateAfter));
                    datas.getUserRepository().saveAndFlush(user1);
                    ref.schId = datas.getFirstRole(user1.getRoles()).getYO().getId();

                    body.wrtr.name("id1").value(user1.getId());
                } else if (inv != null) {
                    inv.setCode(uuid.toString());
                    inv.setExpDate(Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    ref.schId = datas.getFirstRole(inv.getRoles()).getYO().getId();

                    body.wrtr.name("id1").value(inv.getId());
                }
                System.out.println("setCode " + uuid);

                body.wrtr.name("code").value(uuid.toString())
                    .name("id").value(body.id);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("codPepL1C", ans, subscriber.getType(), ref.schId + "", subscriber.getLvlGr(), "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addKid")
    public JsonObject addKid(@RequestBody DataParents body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                User kidU = datas.userById(body.id);
                Invite kidI = datas.inviteById(body.id);
                JsonObject par = body.bod.getAsJsonObject("par");
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if (kidU != null) {
                    body.wrtr.name("id").value(kidU.getId())
                        .name("body").beginObject()
                        .name("name").value(kidU.getFio())
                        .name("login").value(kidU.getLogin())
                        .name("par").beginObject();
                    for (String id : par.keySet()) {
                        Role role = new Role(null, datas.schoolById(Long.parseLong(subscriber.getLvlSch())));
                        datas.getRoleRepository().saveAndFlush(role);
                        Invite inv = new Invite(par.getAsJsonObject(id).get("name").getAsString(), Map.of(
                            1L, role
                        ), Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);

                        body.wrtr.name(inv.getId() + "").beginObject()
                            .name("name").value(inv.getFio());

                        if (!inv.getRoles().get(1L).getKids().contains(kidU)) {
                            inv.getRoles().get(1L).getKids().add(kidU);
                        }
                        kidU.getRoles().get(0L).getParentsInv().add(inv);
                        datas.getUserRepository().saveAndFlush(kidU);

                        body.wrtr.endObject();
                    }
                    body.wrtr.endObject().endObject();
                }
                if (kidI != null) {
                    body.wrtr.name("id").value(kidI.getId())
                        .name("body").beginObject()
                        .name("name").value(kidI.getFio())
                        .name("par").beginObject();
                    for (String id : par.keySet()) {
                        Role role = new Role(null, datas.schoolById(Long.parseLong(subscriber.getLvlSch())));
                        Invite inv = new Invite(par.getAsJsonObject(id).get("name").getAsString(), Map.of(
                            1L, role
                        ), Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);

                        body.wrtr.name(inv.getId() + "").beginObject()
                            .name("name").value(inv.getFio());

                        if (!inv.getRoles().get(1L).getKidsInv().contains(kidI)) {
                            inv.getRoles().get(1L).getKidsInv().add(kidI);
                        }
                        kidI.getRoles().get(0L).getParentsInv().add(inv);
                        datas.getInviteRepository().saveAndFlush(kidI);

                        body.wrtr.endObject();
                    }
                    body.wrtr.endObject().endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addKidC", ans, TypesConnect.PARENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
        }, body.wrtr, body.bol);
    }


    @PostMapping(value = "/getParents")
    public JsonObject getParents(@RequestBody DataParents body) {
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
                    body.wrtr.name("bodyP").beginObject();
                    if (!ObjectUtils.isEmpty(group.getKids())) {
                        for (User studU : group.getKids()) {
                            if (studU == null) continue;
                            body.wrtr.name(studU.getId() + "").beginObject()
                                .name("name").value(studU.getFio())
                                .name("login").value(studU.getLogin());
                            if (!ObjectUtils.isEmpty(studU.getCode())) {
                                body.wrtr.name("link").value(studU.getCode());
                            }
                            body.wrtr.name("par").beginObject();
                            datas.usersByList(studU.getRoles().get(0L).getParents(), true, body.wrtr);
                            datas.invitesByList(studU.getRoles().get(0L).getParentsInv(), true, body.wrtr);
                            body.wrtr.endObject().endObject();
                        }
                    }
                    if (!ObjectUtils.isEmpty(group.getKidsInv())) {
                        for (Invite studI : group.getKidsInv()) {
                            if (studI == null) continue;
                            body.wrtr.name(studI.getId() + "").beginObject()
                                .name("name").value(studI.getFio());
                            if (!ObjectUtils.isEmpty(studI.getCode())) {
                                body.wrtr.name("link").value(studI.getCode());
                            }
                            body.wrtr.name("par").beginObject();
                            datas.usersByList(studI.getRoles().get(0L).getParents(), true, body.wrtr);
                            datas.invitesByList(studI.getRoles().get(0L).getParentsInv(), true, body.wrtr);
                            body.wrtr.endObject().endObject();
                        }
                    }
                    body.wrtr.endObject().name("bodyC").beginObject();
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
    public JsonObject getInfo(@RequestBody DataParents body) {
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
            authController.infCon(body.uuid, null, TypesConnect.PARENTS, "main", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataParents {
    public String login, uuid, name;
    public Long group, id, id1;
    public JsonObject bod;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}