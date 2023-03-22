package ru.mirea.controllers.people;

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
import ru.mirea.data.json.Role;
import ru.mirea.data.models.Group;
import ru.mirea.data.models.School;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

@RestController
@RequestMapping("/parents")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
public class ParentsController {

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
                ans.add("bodyC", bodyAns);
                JsonObject bodA1 = new JsonObject();
                ans.add("bodyP", bodA1);
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = null;
                Long schId = null;
                if(user != null) {
                    schId = datas.getFirstRole(user.getRoles()).getYO();
                    School school = datas.schoolById(schId);
                    if(user.getRoles().containsKey(3L)) {
                        group = datas.groupById(body.get("group").getAsLong());
                    } else {
                        group = datas.groupById(datas.getFirstRole(user.getRoles()).getGroup());
                    }
                    if(group != null && school != null && school.getGroups().contains(group.getId())) {
                        if (!ObjectUtils.isEmpty(group.getKids())) {
                            for (Long i1 : group.getKids()) {
                                JsonObject studO = new JsonObject(), studC, parGL = null, parO;
                                User studU = datas.userById(i1);
                                studO.addProperty("name", studU.getFio());
                                studO.addProperty("login", studU.getLogin());
                                if (!ObjectUtils.isEmpty(studU.getCode())) studO.addProperty("link", studU.getCode());
                                if(studU != null && (!ObjectUtils.isEmpty(studU.getRoles().get(0L).getParentsInv())
                                || !ObjectUtils.isEmpty(studU.getRoles().get(0L).getParents()))){
                                    studC = studO.deepCopy();
                                    bodA1.add(i1 + "", studC);
                                    parGL = new JsonObject();
                                    studC.add("par", parGL);
                                }
                                if(studU != null && !ObjectUtils.isEmpty(studU.getRoles().get(0L).getParents())){
                                    for (Long i2 : studU.getRoles().get(0L).getParents()) {
                                        parO = new JsonObject();
                                        User parU = datas.userById(i2);
                                        parO.addProperty("name", parU.getFio());
                                        parO.addProperty("login", parU.getLogin());
                                        if (!ObjectUtils.isEmpty(parU.getCode())) parO.addProperty("link", parU.getCode());
                                        parGL.add(i2 + "", parO);
                                    }
                                }
                                if(studU != null && !ObjectUtils.isEmpty(studU.getRoles().get(0L).getParentsInv())){
                                    for (Long i2 : studU.getRoles().get(0L).getParentsInv()) {
                                        parO = new JsonObject();
                                        Invite parI = datas.inviteById(i2);
                                        parO.addProperty("name", parI.getFio());
                                        if (!ObjectUtils.isEmpty(parI.getCode())) parO.addProperty("link", parI.getCode());
                                        parGL.add(i2 + "", parO);
                                    }
                                }
                                bodyAns.add(i1 + "", studO);
                            }
                        }
                        if (!ObjectUtils.isEmpty(group.getKidsInv())) {
                            for (Long i1 : group.getKidsInv()) {
                                JsonObject studO = new JsonObject(), studC, parGL = null, parO;
                                Invite studI = datas.inviteById(i1);
                                studO.addProperty("name", studI.getFio());
                                if (!ObjectUtils.isEmpty(studI.getCode())) studO.addProperty("link", studI.getCode());
                                if(studI != null && (!ObjectUtils.isEmpty(studI.getRole().get(0L).getParentsInv())
                                        || !ObjectUtils.isEmpty(studI.getRole().get(0L).getParents()))){
                                    studC = studO.deepCopy();
                                    bodA1.add(i1 + "", studC);
                                    parGL = new JsonObject();
                                    studC.add("par", parGL);
                                }
                                if(studI != null && !ObjectUtils.isEmpty(studI.getRole().get(0L).getParents())){
                                    for (Long i2 : studI.getRole().get(0L).getParents()) {
                                        parO = new JsonObject();
                                        User parU = datas.userById(i2);
                                        parO.addProperty("name", parU.getFio());
                                        parO.addProperty("login", parU.getLogin());
                                        if (!ObjectUtils.isEmpty(parU.getCode())) parO.addProperty("link", parU.getCode());
                                        parGL.add(i2 + "", parO);
                                    }
                                }
                                if(studI != null && !ObjectUtils.isEmpty(studI.getRole().get(0L).getParentsInv())){
                                    for (Long i2 : studI.getRole().get(0L).getParentsInv()) {
                                        parO = new JsonObject();
                                        Invite parI = datas.inviteById(i2);
                                        parO.addProperty("name", parI.getFio());
                                        if (!ObjectUtils.isEmpty(parI.getCode())) parO.addProperty("link", parI.getCode());
                                        parGL.add(i2 + "", parO);
                                    }
                                }
                                bodyAns.add(i1 + "", studO);
                            }
                        }
                    }
                }
                if(group == null){
                    ans.addProperty("error", true);
                } else {
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.PARENTS, schId+"", group.getId()+"", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
                }
                return ans;
            }
            case "addKid" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Invite inv = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    User kidU = datas.userById(body.get("id").getAsLong());
                    Invite kidI = datas.inviteById(body.get("id").getAsLong());
                    JsonObject par = body.get("bod").getAsJsonObject().get("par").getAsJsonObject(), parA = new JsonObject();
                    if(kidU != null) {
                        ans.addProperty("id", kidU.getId());
                        bodyAns.addProperty("name", kidU.getFio());
                        bodyAns.addProperty("login", kidU.getLogin());
                        bodyAns.add("par", parA);
                        for (String id : par.keySet()) {
                            Instant after = Instant.now().plus(Duration.ofDays(30));
                            Date dateAfter = Date.from(after);
                            inv = new Invite(par.get(id).getAsJsonObject().get("name").getAsString(), new HashMap<>() {{
                                put(1L, new Role(null, Long.parseLong(subscriber.getLvlSch()), null));
                            }}, Main.df.format(dateAfter));
                            datas.getInviteRepository().saveAndFlush(inv);

                            JsonObject parO = new JsonObject();
                            parO.addProperty("name", inv.getFio());
                            parA.add(inv.getId()+"", parO);

                            if (kidU.getRoles().get(0L).getParentsInv() == null) kidU.getRoles().get(0L).setParentsInv(new ArrayList<>(asList(kidU.getId())));
                            kidU.getRoles().get(0L).getParentsInv().add(inv.getId());
                            datas.getUserRepository().saveAndFlush(kidU);
                        }
                    }
                    if(kidI != null) {
                        ans.addProperty("id", kidI.getId());
                        bodyAns.addProperty("name", kidI.getFio());
                        bodyAns.add("par", parA);
                        for (String id : par.keySet()) {
                            Instant after = Instant.now().plus(Duration.ofDays(30));
                            Date dateAfter = Date.from(after);
                            inv = new Invite(par.get(id).getAsJsonObject().get("name").getAsString(), new HashMap<>() {{
                                put(1L, new Role(null, Long.parseLong(subscriber.getLvlSch()), null));
                            }}, Main.df.format(dateAfter));
                            datas.getInviteRepository().saveAndFlush(inv);

                            JsonObject parO = new JsonObject();
                            parO.addProperty("name", inv.getFio());
                            parA.add(inv.getId()+"", parO);

                            if (kidI.getRole().get(0L).getParentsInv() == null) kidI.getRole().get(0L).setParentsInv(new ArrayList<>(asList(kidI.getId())));
                            kidI.getRole().get(0L).getParentsInv().add(inv.getId());
                            datas.getInviteRepository().saveAndFlush(kidI);
                        }
                    }
                }
                if(inv == null){
                    ans.addProperty("error", true);
                } else {
//                    ans.addProperty("id", inv.getId());
//                    bodyAns.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("addKidC", ans, TypesConnect.PARENTS, subscriber.getLvlSch(), subscriber.getLvlGr(), "main", "main");
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
                    authController.sendMessageForAll("codPepL1C", ans, subscriber.getType(), schId+"", subscriber.getLvlGr(), "ht", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chPep" -> {
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