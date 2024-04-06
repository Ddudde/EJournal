package ru.mirea.controllers.people;

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
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.MainService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RequestMapping("/admins")
@NoArgsConstructor
@RestController public class AdminsController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/remPep")
    public JsonObject remPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        Syst syst = datas.getDbService().getSyst();
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && syst != null && user1 != null) {
                user1.getRoles().remove(4L);
                datas.getDbService().getUserRepository().saveAndFlush(user1);
                syst.getAdmins().remove(user1);
                datas.getDbService().getSystRepository().saveAndFlush(syst);

                body.wrtr.name("id").value(user1.getId());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("remPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPep")
    public JsonObject chPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userById(body.id);
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && user1 != null) {
                user1.setFio(body.name);
                datas.getDbService().getUserRepository().saveAndFlush(user1);

                body.wrtr.name("id").value(user1.getId())
                    .name("name").value(body.name);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("chPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addPep")
    public JsonObject addPep(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Syst syst = datas.getDbService().getSyst();
        try {
            body.wrtr = datas.init(body.toString());
            if (user != null && user.getRoles().containsKey(4L) && syst != null) {
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null));
                User inv = new User(body.name, Map.of(
                    4L, role
                ), Main.df.format(dateAfter));
                datas.getDbService().getUserRepository().saveAndFlush(inv);
                syst.getAdmins().add(inv);
                datas.getDbService().getSystRepository().saveAndFlush(syst);

                body.wrtr.name("id").value(inv.getId())
                    .name("body").beginObject()
                    .name("name").value(body.name)
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("addPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getAdmins")
    public JsonObject getAdmins(@RequestBody DataAdmins body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Syst syst = datas.getDbService().getSyst();
        try {
            body.wrtr = datas.init(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && syst != null) {
                datas.usersByList(syst.getAdmins(), true, body.wrtr);
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
    public String login, uuid, name;
    public Long id;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}