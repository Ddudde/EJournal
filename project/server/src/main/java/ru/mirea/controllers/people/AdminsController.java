package ru.mirea.controllers.people;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.json.Role;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequestMapping("/admins")
@NoArgsConstructor
@RestController public class AdminsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        Syst syst = datas.getSyst();
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && syst != null) {
                if (user1 != null) {
                    user1.getRoles().remove(4L);
                    datas.getUserRepository().saveAndFlush(user1);
                    syst.getAdmins().remove(user1.getId());
                    datas.getSystRepository().saveAndFlush(syst);

                    body.wrtr.name("id").value(user1.getId());
                } else if (inv != null) {
                    datas.getInviteRepository().delete(inv);
                    syst.getAdminsInv().remove(inv.getId());
                    datas.getSystRepository().saveAndFlush(syst);

                    body.wrtr.name("id").value(inv.getId());
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("remPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && (user1 != null || inv != null)) {
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
            authController.sendMessageForAll("chPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addPep")
    public JsonObject addPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        Syst syst = datas.getSyst();
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && syst != null) {
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                Invite inv = new Invite(body.name, Map.of(
                    4L, new Role(null)
                ), Main.df.format(dateAfter));
                datas.getInviteRepository().saveAndFlush(inv);
                syst.getAdminsInv().add(inv.getId());
                datas.getSystRepository().saveAndFlush(syst);

                body.wrtr.name("id").value(inv.getId())
                    .name("body").beginObject()
                    .name("name").value(body.name)
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getAdmins")
    public JsonObject getAdmins(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        Syst syst = datas.getSyst();
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && syst != null) {
                datas.usersByList(syst.getAdmins(), body.wrtr, true);
                datas.invitesByList(syst.getAdminsInv(), body.wrtr, true);
            }
            body.wrtr.endObject();
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.ADMINS, "null", "main", user.getRoles().containsKey(4L) ? "adm" : "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataAdmins {
    public String login, uuid, name, id;
    public Long id1;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}