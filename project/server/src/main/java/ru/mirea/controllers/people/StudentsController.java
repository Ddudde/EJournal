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
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/students")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
public class StudentsController {

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
            case "getStud" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(subscriber.getLogin());
                Group group = null;
                Long schId = null;
                if(user != null) {
                    schId = datas.getFirstRole(user.getRoles()).getYO();
                    School school = datas.schoolById(schId);
                    group = datas.groupById(body.get("group").getAsLong());
                    if(group != null && school != null && school.getGroups().contains(group.getId())) {
                        if (!ObjectUtils.isEmpty(group.getKids())) {
                            for (Long i1 : group.getKids()) {
                                JsonObject studO = new JsonObject();
                                User studU = datas.userById(i1);
                                studO.addProperty("name", studU.getFio());
                                studO.addProperty("login", studU.getLogin());
                                if (!ObjectUtils.isEmpty(studU.getCode())) studO.addProperty("link", studU.getCode());
                                bodyAns.add(i1 + "", studO);
                            }
                        }
                        if (!ObjectUtils.isEmpty(group.getKidsInv())) {
                            for (Long i1 : group.getKidsInv()) {
                                JsonObject studO = new JsonObject();
                                Invite studI = datas.inviteById(i1);
                                studO.addProperty("name", studI.getFio());
                                if (!ObjectUtils.isEmpty(studI.getCode())) studO.addProperty("link", studI.getCode());
                                bodyAns.add(i1 + "", studO);
                            }
                        }
                    }
                }
                if(group == null){
                    ans.addProperty("error", true);
                } else {
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.STUDENTS, schId+"_"+ group.getId(), user.getRoles().containsKey(3L) ? "ht" : "main");
                }
                return ans;
            }
            case "addPep" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Syst syst = datas.getSyst();
                if(user != null && user.getRoles().containsKey(4L) && syst != null) {
                    Instant after = Instant.now().plus(Duration.ofDays(30));
                    Date dateAfter = Date.from(after);
                    Invite inv = new Invite(body.get("name").getAsString(), new HashMap<>(){{
                        put(4L, new Role(null));
                    }}, Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    if(syst.getAdminsInv() == null) syst.setAdminsInv(new ArrayList<>());
                    syst.getAdminsInv().add(inv.getId());
                    datas.getSystemRepository().saveAndFlush(syst);

                    ans.addProperty("id", inv.getId());
                    bodyAns.addProperty("name", body.get("name").getAsString());

                    authController.sendMessageForAll("addPepC", ans, TypesConnect.STUDENTS, subscriber.getPodTypeL1(), "main");
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
                if(user != null && user.getRoles().containsKey(4L) && (user1 != null || inv != null)) {
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

                    authController.sendMessageForAll("chPepC", ans, TypesConnect.STUDENTS, subscriber.getPodTypeL1(), "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "remPep" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Syst syst = datas.getSyst();
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && (user1 != null || inv != null) && syst != null) {
                    if(user1 != null){
                        user1.getRoles().remove(4L);
                        datas.getUserRepository().saveAndFlush(user1);
                        if(!ObjectUtils.isEmpty(syst.getAdmins())) syst.getAdmins().remove(user1.getId());
                        datas.getSystemRepository().saveAndFlush(syst);

                        ans.addProperty("id", user1.getId());
                    } else if(inv != null){
                        datas.getInviteRepository().delete(inv);
                        if(!ObjectUtils.isEmpty(syst.getAdminsInv())) syst.getAdminsInv().remove(inv.getId());
                        datas.getSystemRepository().saveAndFlush(syst);

                        ans.addProperty("id", inv.getId());
                    }

                    authController.sendMessageForAll("remPepC", ans, TypesConnect.STUDENTS, subscriber.getPodTypeL1(), "main");
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