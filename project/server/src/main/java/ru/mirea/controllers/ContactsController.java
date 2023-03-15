package ru.mirea.controllers;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.School;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.SystemRepository;
import ru.mirea.data.reps.auth.UserRepository;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/contacts")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
public class ContactsController {

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
            case "getContacts" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                bodyAns = new JsonObject();
                JsonObject map = new JsonObject();
                Long conId = null, schId = null;
                if(subscriber != null) {
                    ans.add("body", bodyAns);
                    bodyAns.add("mapPr", map);
                    User user = datas.userByLogin(subscriber.getLogin());
                    Syst syst = datas.getSyst();
                    if (user != null) {
                        schId = user.getRoles().get(body.get("role").getAsLong()).getYO();
                        School school = datas.schoolById(schId);
                        if (Objects.equals(body.get("type").getAsString(), "Yo") && school != null) {
                            conId = school.getContacts();
                        }
                    }
                    if (Objects.equals(body.get("type").getAsString(), "Por") && syst != null) {
                        conId = syst.getContacts();
                        schId = null;
                    }
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.CONTACTS, body.get("type").getAsString(), schId + "");
                }
                if(conId != null) {
                    Contacts conM = datas.contactsById(conId);
                    bodyAns.addProperty("contact", conM.getContact());
                    map.addProperty("text", conM.getText());
                    map.addProperty("imgUrl", conM.getImgUrl());
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chContact" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                Syst syst = datas.getSyst();
                Contacts contacts = null;
                if(user != null) {
                    if(user.getRoles().containsKey(4L) && Objects.equals(subscriber.getPodTypeL1(), "Por")){
                        contacts = datas.contactsById(syst.getContacts());
                    }
                    if(user.getRoles().containsKey(3L) && Objects.equals(subscriber.getPodTypeL1(), "Yo")){
                        Long schId = user.getRoles().get(body.get("role").getAsLong()).getYO();
                        School school = datas.schoolById(schId);
                        if(school != null) {
                            contacts = datas.contactsById(school.getContacts());
                        }
                    }
                }
                if(contacts != null) {
                    String p = body.get("p").getAsString();
                    String p1 = body.has("p1") ? body.get("p1").getAsString() : null;
                    if(Objects.equals(p, "contact")) {
                        contacts.setContact(body.get("val").getAsString());
                    }
                    if(Objects.equals(p, "mapPr")) {
                        if(Objects.equals(p1, "text")) {
                            contacts.setText(body.get("val").getAsString());
                        }
                        if(Objects.equals(p1, "imgUrl")) {
                            contacts.setImgUrl(body.get("val").getAsString());
                        }
                    }
                    datas.getContactsRepository().saveAndFlush(contacts);
                    ans.add("val", body.get("val"));
                    ans.add("p", body.get("p"));
                    ans.add("p1", body.get("p1"));

                    authController.sendMessageForAll("chContactC", ans, TypesConnect.CONTACTS, subscriber.getPodTypeL1(), subscriber.getPodTypeL2());
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