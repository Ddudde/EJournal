package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.util.Objects;

@RequestMapping("/contacts")
@NoArgsConstructor
@RestController public class ContactsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/chContact")
    public JsonObject chContact(@RequestBody DataContacts body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Syst syst = datas.getDbService().getSyst();
        Contacts contacts = null;
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                if(user.getRoles().containsKey(4L) && Objects.equals(subscriber.getLvlMore2(), "Por")){
                    contacts = syst.getContacts();
                }
                if(user.getRoles().containsKey(3L) && Objects.equals(subscriber.getLvlMore2(), "Yo")){
                    School school = user.getRoles().get(user.getSelRole()).getYO();
                    if(school != null) contacts = school.getContacts();
                }
            }
            if(contacts != null) {
                if(Objects.equals(body.p, "contact")) {
                    contacts.setContact(body.val);
                }
                if(Objects.equals(body.p, "mapPr")) {
                    if(Objects.equals(body.p1, "text")) {
                        contacts.setText(body.val);
                    }
                    if(Objects.equals(body.p1, "imgUrl")) {
                        contacts.setImgUrl(body.val);
                    }
                }
                datas.getDbService().getContactsRepository().saveAndFlush(contacts);
                body.wrtr.name("val").value(body.val)
                    .name("p").value(body.p)
                    .name("p1").value(body.p1);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chContactC", ans, TypesConnect.CONTACTS, subscriber.getLvlSch(), "main", "main", subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getContacts")
    public JsonObject getContacts(@RequestBody DataContacts body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        final var ref = new Object() {
            Long schId = null, conId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(subscriber != null) {
                User user = datas.getDbService().userByLogin(subscriber.getLogin());
                Syst syst = datas.getDbService().getSyst();
                if (Objects.equals(body.type, "Yo") && user != null) {
                    School school = user.getRoles().get(user.getSelRole()).getYO();
                    ref.schId = school.getId();
                    if(school != null) ref.conId = school.getContacts().getId();
                }
                if (Objects.equals(body.type, "Por") && syst != null) {
                    ref.conId = syst.getContacts().getId();
                    ref.schId = null;
                }
                if(ref.conId != null) {
                    Contacts conM = datas.getDbService().contactsById(ref.conId);
                    body.wrtr.name("body").beginObject()
                        .name("contact").value(conM.getContact())
                        .name("mapPr").beginObject()
                        .name("text").value(conM.getText())
                        .name("imgUrl").value(conM.getImgUrl())
                        .endObject().endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.CONTACTS, ref.schId + "", "main", "main", body.type);
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataContacts {
    public String uuid, type, p, p1, val;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}