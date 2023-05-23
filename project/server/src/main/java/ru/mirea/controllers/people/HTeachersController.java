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
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/hteachers")
@NoArgsConstructor
@RestController public class HTeachersController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remGroup")
    public JsonObject remGroup(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                ref.schId = user.getRoles().get(3L).getYO();
                School school = datas.schoolById(ref.schId);
                if (school != null) {
                    Group group = datas.groupById(body.grId);
                    datas.getGroupRepository().delete(group);
                    school.getGroups().remove(group.getId());
                    datas.getSchoolRepository().saveAndFlush(school);

                    body.wrtr.name("id").value(group.getId());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("remGroupC", ans, TypesConnect.MAIN, ref.schId + "", "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addGroup")
    public JsonObject addGroup(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                ref.schId = user.getRoles().get(3L).getYO();
                School school = datas.schoolById(ref.schId);
                if (school != null) {
                    Group group = new Group(body.name);
                    datas.getGroupRepository().saveAndFlush(group);
                    school.getGroups().add(group.getId());
                    datas.getSchoolRepository().saveAndFlush(school);

                    body.wrtr.name("id").value(group.getId())
                        .name("name").value(body.name);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addGroupC", ans, TypesConnect.MAIN, ref.schId + "", "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chGroup")
    public JsonObject chGroup(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(3L)) {
                ref.schId = user.getRoles().get(3L).getYO();
                School school = datas.schoolById(ref.schId);
                if (school != null) {
                    Group group = datas.groupById(body.grId);
                    group.setName(body.name);
                    datas.getGroupRepository().saveAndFlush(group);

                    body.wrtr.name("id").value(group.getId())
                        .name("name").value(body.name);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chGroupC", ans, TypesConnect.MAIN, ref.schId + "", "main", "ht", "main");
        }, body.wrtr, body.bol);
    }

//    @PostMapping(value = "/getGroups")
//    public JsonObject getGroups(@RequestBody DataHTeachers body) {
//        Subscriber subscriber = authController.getSubscriber(body.uuid);
//        User user = datas.userByLogin(subscriber.getLogin());
//        try {
//            body.wrtr = datas.ini(body.toString());
//            if (user != null && (user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L))) {
//                body.wrtr.name("body").beginObject();
//                datas.groupsByUser(user, body.wrtr);
//            }
//        } catch (Exception e) {body.bol = Main.excp(e);}
//        return datas.getObj(ans -> {}, body.wrtr, body.bol);
//    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        final var ref = new Object() {
            School sch = null;
        };
        if (user1 != null) {
            ref.sch = datas.schoolById(user1.getRoles().get(3L).getYO());
        } else if (inv != null) {
            ref.sch = datas.schoolById(inv.getRole().get(3L).getYO());
        }
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && ref.sch != null) {
                if ((user.getSelRole() == 4L && user.getRoles().containsKey(4L)
                    || user.getSelRole() == 3L && user.getRoles().containsKey(3L))
                    && (user1 != null || inv != null)) {
                    if (user1 != null) {
                        user1.setFio(body.name);
                        datas.getUserRepository().saveAndFlush(user1);

                        body.wrtr.name("id").value(user1.getId());
                    } else if (inv != null) {
                        inv.setFio(body.name);
                        datas.getInviteRepository().saveAndFlush(inv);

                        body.wrtr.name("id").value(inv.getId());
                    }
                    body.wrtr.name("id1").value(ref.sch.getId())
                        .name("name").value(body.name);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            if (user.getSelRole() == 4L) {
                authController.sendMessageForAll("chInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, ref.sch.getId() + "", "main", "user", "main");
                authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, ref.sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == 3L) {
                authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                authController.sendMessageForAll("chInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        final var ref = new Object() {
            School sch = null;
        };
        if (user1 != null) {
            ref.sch = datas.schoolById(user1.getRoles().get(3L).getYO());
        } else if (inv != null) {
            ref.sch = datas.schoolById(inv.getRole().get(3L).getYO());
        }
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && ref.sch != null) {
                if ((user.getSelRole() == 4L && user.getRoles().containsKey(4L)
                    || user.getSelRole() == 3L && user.getRoles().containsKey(3L))
                    && (user1 != null || inv != null)) {
                    if (user1 != null) {
                        user1.getRoles().remove(3L);
                        datas.getUserRepository().saveAndFlush(user1);
                        ref.sch.getHteachers().remove(user1.getId());
                        datas.getSchoolRepository().saveAndFlush(ref.sch);

                        body.wrtr.name("id").value(user1.getId());
                    } else if (inv != null) {
                        datas.getInviteRepository().delete(inv);
                        ref.sch.getHteachersInv().remove(inv.getId());
                        datas.getSchoolRepository().saveAndFlush(ref.sch);

                        body.wrtr.name("id").value(inv.getId());
                    }
                    body.wrtr.name("id1").value(ref.sch.getId());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            if (user.getSelRole() == 4L) {
                authController.sendMessageForAll("remInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, ref.sch.getId() + "", "main", "user", "main");
                authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, ref.sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == 3L) {
                authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                authController.sendMessageForAll("remInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addPep")
    public JsonObject addPep(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = body.yo;
        };
        if (user.getSelRole() != 4L) {
            ref.schId = user.getRoles().get(3L).getYO();
        }
        School sch = datas.schoolById(ref.schId);
        Invite inv = null;
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && sch != null) {
                if (user.getSelRole() == 3L && user.getRoles().containsKey(3L)
                    || user.getSelRole() == 4L && user.getRoles().containsKey(4L)) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    inv = new Invite(body.name, Map.of(
                        3L, new Role(null, ref.schId)
                    ), Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    sch.getHteachersInv().add(inv.getId());
                    datas.getSchoolRepository().saveAndFlush(sch);

                    body.wrtr.name("id1").value(sch.getId())
                        .name("id").value(inv.getId())
                        .name("body").beginObject()
                        .name("name").value(body.name).endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            if (user.getSelRole() == 4L) {
                authController.sendMessageForAll("addInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, ref.schId + "", "main", "user", "main");
                authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, ref.schId + "", "main", "ht", "main");
            }
            if (user.getSelRole() == 3L) {
                authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
                authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                authController.sendMessageForAll("addInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chSch")
    public JsonObject chSch(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        School school = datas.schoolById(body.schId);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && school != null) {
                school.setName(body.name);
                datas.getSchoolRepository().saveAndFlush(school);

                body.wrtr.name("id").value(body.schId)
                    .name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addSch")
    public JsonObject addSch(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L)) {
                School school = new School(body.name);
                datas.getSchoolRepository().saveAndFlush(school);
                body.wrtr.name("id").value(school.getId())
                    .name("body").beginObject()
                    .name("name").value(body.name)
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/remSch")
    public JsonObject remSch(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        School school = datas.schoolById(body.schId);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && school != null) {
                datas.getSchoolRepository().delete(school);

                body.wrtr.name("id").value(body.schId);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", subscriber.getLvlMore1(), "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataHTeachers body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
            String l2 = "user";
        };
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null) {
                if (user.getSelRole() == 4L && user.getRoles().containsKey(4L)) {
                    ref.l2 = "adm";
                    for (School el : datas.getSchools()) {
                        body.wrtr.name(el.getId() + "").beginObject()
                            .name("name").value(el.getName())
                            .name("pep").beginObject();
                        datas.usersByList(el.getHteachers(), body.wrtr, true);
                        datas.invitesByList(el.getHteachersInv(), body.wrtr, true);
                        body.wrtr.endObject().endObject();
                    }
                } else {
                    ref.l2 = "ht";
                    ref.schId = user.getRoles().get(user.getSelRole()).getYO();
                    School school = datas.schoolById(ref.schId);
                    if (school != null) {
                        datas.usersByList(school.getHteachers(), body.wrtr, true);
                        datas.invitesByList(school.getHteachersInv(), body.wrtr, true);
                    }
                }
            }
            body.wrtr.endObject();
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.HTEACHERS, ref.schId + "", "main", ref.l2, "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataHTeachers {
    public String login, uuid, name, id;
    public Long schId, yo, id1, grId;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}