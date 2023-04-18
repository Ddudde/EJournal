package ru.mirea.controllers.people;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.json.Role;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/hteachers")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000", "https://ddudde.github.io"})
public class HTeachersController {

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
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(subscriber.getLogin());
                JsonObject htO = null;
                Long schId = null;
                if(user != null) {
                    if(body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L)) {
                        for(School el : datas.getSchools()){
                            JsonObject schools = new JsonObject();
                            schools.addProperty("name", el.getName());
                            JsonObject pep = new JsonObject();
                            schools.add("pep", pep);
                            if(!ObjectUtils.isEmpty(el.getHteachers())){
                                for(Long i1 : el.getHteachers()){
                                    htO = new JsonObject();
                                    User htU = datas.userById(i1);
                                    htO.addProperty("name", htU.getFio());
                                    htO.addProperty("login", htU.getLogin());
                                    System.out.println(htU.getCode());
                                    if(!ObjectUtils.isEmpty(htU.getCode())) htO.addProperty("link", htU.getCode());
                                    pep.add(i1+"", htO);
                                }
                            }
                            if(!ObjectUtils.isEmpty(el.getHteachersInv())){
                                for(Long i1 : el.getHteachersInv()){
                                    htO = new JsonObject();
                                    Invite htI = datas.inviteById(i1);
                                    htO.addProperty("name", htI.getFio());
                                    System.out.println(htI.getCode());
                                    if(!ObjectUtils.isEmpty(htI.getCode())) htO.addProperty("link", htI.getCode());
                                    pep.add(i1+"", htO);
                                }
                            }
                            bodyAns.add(el.getId()+"", schools);
                        }
                    } else {
                        schId = user.getRoles().get(body.get("role").getAsLong()).getYO();
                        School school = datas.schoolById(schId);
                        if(school != null) {
                            if (!ObjectUtils.isEmpty(school.getHteachers())) {
                                for (Long i1 : school.getHteachers()) {
                                    htO = new JsonObject();
                                    User htU = datas.userById(i1);
                                    htO.addProperty("name", htU.getFio());
                                    htO.addProperty("login", htU.getLogin());
                                    System.out.println(htU.getCode());
                                    if (!ObjectUtils.isEmpty(htU.getCode())) htO.addProperty("link", htU.getCode());
                                    bodyAns.add(i1 + "", htO);
                                }
                            }
                            if (!ObjectUtils.isEmpty(school.getHteachersInv())) {
                                for (Long i1 : school.getHteachersInv()) {
                                    htO = new JsonObject();
                                    Invite htI = datas.inviteById(i1);
                                    htO.addProperty("name", htI.getFio());
                                    System.out.println(htI.getCode());
                                    if (!ObjectUtils.isEmpty(htI.getCode())) htO.addProperty("link", htI.getCode());
                                    bodyAns.add(i1 + "", htO);
                                }
                            }
                        }
                    }
                }
                if(htO == null){
                    ans.addProperty("error", true);
                } else {
                    String l2 = "";
                    if(body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L)){
                        l2 = "adm";
                    } else if(body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L)){
                        l2 = "ht";
                    } else {
                        l2 = "user";
                    }
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.HTEACHERS, schId+"", "main", l2, "main");
                }
                return ans;
            }
            case "addSch" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                if(user != null && user.getRoles().containsKey(4L)) {
                    School school = new School(body.get("name").getAsString());
                    datas.getSchoolRepository().saveAndFlush(school);

                    ans.addProperty("id", school.getId());
                    bodyAns.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "remSch" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                School school = datas.schoolById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && school != null) {
                    datas.getSchoolRepository().delete(school);

                    ans.addProperty("id", body.get("id").getAsLong());

                    authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chSch" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                School school = datas.schoolById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && school != null) {
                    school.setName(body.get("name").getAsString());
                    datas.getSchoolRepository().saveAndFlush(school);

                    ans.addProperty("id", body.get("id").getAsLong());
                    ans.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "addPep" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Long schId = null;
                if(body.get("role").getAsLong() == 4L){
                    schId = body.get("yo").getAsLong();
                } else {
                    schId = user.getRoles().get(3L).getYO();
                }
                School sch = datas.schoolById(schId);
                Invite inv = null;
                if(user != null && sch != null) {
                    if(body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L)
                            || body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L)) {
                        Instant after = Instant.now().plus(Duration.ofDays(30));
                        Date dateAfter = Date.from(after);
                        Long finalSchId = schId;
                        inv = new Invite(body.get("name").getAsString(), new HashMap<>() {{
                            put(3L, new Role(null, finalSchId));
                        }}, Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);
                        if (sch.getHteachersInv() == null) sch.setHteachersInv(new ArrayList<>());
                        sch.getHteachersInv().add(inv.getId());
                        datas.getSchoolRepository().saveAndFlush(sch);

                        ans.addProperty("id1", sch.getId());
                        ans.addProperty("id", inv.getId());
                        bodyAns.addProperty("name", body.get("name").getAsString());
                    }
                }
                if(inv == null) {
                    ans.addProperty("error", true);
                } else {
                    if(body.get("role").getAsLong() == 4L) {
                        authController.sendMessageForAll("addInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, schId+"", "main", "user", "main");
                        authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, schId+"", "main", "ht", "main");
                    }
                    if(body.get("role").getAsLong() == 3L) {
                        authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("addInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                        authController.sendMessageForAll("addInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
                    }
                }
                return ans;
            }
            case "remPep" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                School sch = null;
                if(user1 != null){
                    sch = datas.schoolById(user1.getRoles().get(3L).getYO());
                } else if(inv != null){
                    sch = datas.schoolById(inv.getRole().get(3L).getYO());
                }
                if(user != null && sch != null) {
                    if((body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L)
                            || body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L))
                            && (user1 != null || inv != null)) {
                        if (user1 != null) {
                            user1.getRoles().remove(3L);
                            datas.getUserRepository().saveAndFlush(user1);
                            if (!ObjectUtils.isEmpty(sch.getHteachers())) sch.getHteachers().remove(user1.getId());
                            datas.getSchoolRepository().saveAndFlush(sch);

                            ans.addProperty("id", user1.getId());
                        } else if (inv != null) {
                            datas.getInviteRepository().delete(inv);
                            if (!ObjectUtils.isEmpty(sch.getHteachersInv())) sch.getHteachersInv().remove(inv.getId());
                            datas.getSchoolRepository().saveAndFlush(sch);

                            ans.addProperty("id", inv.getId());
                        }
                        ans.addProperty("id1", sch.getId());
                    }
                }
                if(ans.has("id")){
                    if(body.get("role").getAsLong() == 4L) {
                        authController.sendMessageForAll("remInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId()+"", "main", "user", "main");
                        authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId()+"", "main", "ht", "main");
                    }
                    if(body.get("role").getAsLong() == 3L) {
                        authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("remInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                        authController.sendMessageForAll("remInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
                    }
                } else{
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chPep" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                School sch = null;
                if(user1 != null){
                    sch = datas.schoolById(user1.getRoles().get(3L).getYO());
                } else if(inv != null){
                    sch = datas.schoolById(inv.getRole().get(3L).getYO());
                }
                if(user != null && sch != null) {
                    if((body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L)
                            || body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L))
                            && (user1 != null || inv != null)) {
                        if (user1 != null) {
                            user1.setFio(body.get("name").getAsString());
                            datas.getUserRepository().saveAndFlush(user1);

                            ans.addProperty("id", user1.getId());
                        } else if (inv != null) {
                            inv.setFio(body.get("name").getAsString());
                            datas.getInviteRepository().saveAndFlush(inv);

                            ans.addProperty("id", inv.getId());
                        }
                        ans.addProperty("id1", sch.getId());
                        ans.addProperty("name", body.get("name").getAsString());
                    }
                }
                if(ans.has("id")){
                    if(body.get("role").getAsLong() == 4L) {
                        authController.sendMessageForAll("chInfoL2C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId()+"", "main", "user", "main");
                        authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId()+"", "main", "ht", "main");
                    }
                    if(body.get("role").getAsLong() == 3L) {
                        authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                        authController.sendMessageForAll("chInfoL1C", ans, TypesConnect.HTEACHERS, subscriber.getLvlSch(), "main", "user", "main");
                        authController.sendMessageForAll("chInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
                    }
                } else{
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "getGroups" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                if(user != null && (user.getRoles().containsKey(2L) || user.getRoles().containsKey(3L))) {
                    JsonObject grps = new JsonObject();
                    datas.groupsByUser(user, grps);
                    ans.add("body", grps);
                }
                if(!ans.has("body")){
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "addGroup" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = null;
                Long schId = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    schId = user.getRoles().get(3L).getYO();
                    School school = datas.schoolById(schId);
                    if(school != null) {
                        group = new Group(body.get("name").getAsString());
                        datas.getGroupRepository().saveAndFlush(group);
                        if (school.getGroups() == null) school.setGroups(new ArrayList<>());
                        school.getGroups().add(group.getId());
                        datas.getSchoolRepository().saveAndFlush(school);
                    }
                }
                if(group == null){
                    ans.addProperty("error", true);
                } else {
                    ans.addProperty("id", group.getId());
                    ans.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("addGroupC", ans, TypesConnect.MAIN, schId+"", "main", "ht", "main");
                }
                return ans;
            }
            case "chGroup" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = null;
                Long schId = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    schId = user.getRoles().get(3L).getYO();
                    School school = datas.schoolById(schId);
                    if(school != null) {
                        group = datas.groupById(body.get("id").getAsLong());
                        group.setName(body.get("name").getAsString());
                        datas.getGroupRepository().saveAndFlush(group);
                    }
                }
                if(group == null){
                    ans.addProperty("error", true);
                } else {
                    ans.addProperty("id", group.getId());
                    ans.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("chGroupC", ans, TypesConnect.MAIN, schId+"", "main", "ht", "main");
                }
                return ans;
            }
            case "remGroup" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = null;
                Long schId = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    schId = user.getRoles().get(3L).getYO();
                    School school = datas.schoolById(schId);
                    if(school != null) {
                        group = datas.groupById(body.get("id").getAsLong());
                        datas.getGroupRepository().delete(group);
                        school.getGroups().remove(group.getId());
                        datas.getSchoolRepository().saveAndFlush(school);
                    }
                }
                if(group == null){
                    ans.addProperty("error", true);
                } else {
                    ans.addProperty("id", group.getId());

                    authController.sendMessageForAll("remGroupC", ans, TypesConnect.MAIN, schId+"", "main", "ht", "main");
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