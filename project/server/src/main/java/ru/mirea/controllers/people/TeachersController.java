package ru.mirea.controllers.people;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
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
import java.util.HashMap;
import java.util.UUID;

@RequestMapping("/teachers")
@NoArgsConstructor
@RestController public class TeachersController {

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
            case "getTeachers" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(subscriber.getLogin());
                Long schId = null;
                if(user != null) {
                    schId = datas.getFirstRole(user.getRoles()).getYO();
                    School school = datas.schoolById(schId);
                    if(school != null) {
                        if (user.getRoles().containsKey(3L)) {
                            datas.teachersBySchool(school, bodyAns);
                        } else {

                        }
                    }
                }
                if(ObjectUtils.isEmpty(bodyAns.keySet())){
                    ans.addProperty("error", true);
                } else {
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.TEACHERS, schId+"", "main", user.getRoles().containsKey(3L) ? "ht" : "main", "main");
                }
                return ans;
            }
            case "addTea" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Invite inv = null;
                if(user != null && user.getRoles().containsKey(3L)) {
                    School school = datas.schoolById(Long.parseLong(subscriber.getLvlSch()));
                    if(school != null) {
                        Instant after = Instant.now().plus(Duration.ofDays(30));
                        Date dateAfter = Date.from(after);
                        inv = new Invite(body.get("name").getAsString(), new HashMap<>() {{
                            put(2L, new Role(null, null, Long.parseLong(subscriber.getLvlSch())));
                        }}, Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);
                        school.getTeachersInv().add(inv.getId());
                        datas.getSchoolRepository().saveAndFlush(school);
                    }
                }
                if(inv == null){
                    ans.addProperty("error", true);
                } else {
                    ans.addProperty("id", inv.getId());
                    ans.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("addTeaC", ans, TypesConnect.TEACHERS, subscriber.getLvlSch(), "main", "ht", "main");
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
                    authController.sendMessageForAll("codPepL1C", ans, subscriber.getType(), schId+"", "main", "ht", "main");
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