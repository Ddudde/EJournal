package ru.mirea.controllers.main;

import com.google.gson.Gson;
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
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.util.Objects;

@RequestMapping("/profiles")
@NoArgsConstructor
@RestController public class ProfileController {

    @Autowired
    private Gson gson;

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/chEmail")
    public JsonObject chEmail(@RequestBody DataProfile body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        if(ObjectUtils.isEmpty(body.login)) {
            body.login = subscriber.getLogin();
        }
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && Objects.equals(body.login, subscriber.getLogin())) {
                user.getRoles().get(user.getSelRole()).setEmail(body.email);
                datas.getDbService().getUserRepository().saveAndFlush(user);
                body.wrtr.name("email").value(body.email)
                    .name("role").value(user.getSelRole());
            }
            body.wrtr.endObject();
        } catch (Exception e) { body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chEmail", ans, TypesConnect.PROFILES, "main", "main", "main", user.getLogin());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chInfo")
    public JsonObject chInfo(@RequestBody DataProfile body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        if(ObjectUtils.isEmpty(body.login)) {
            body.login = subscriber.getLogin();
        }
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && Objects.equals(body.login, subscriber.getLogin())) {
                SettingUser settingUser = user.getSettings();
                settingUser.setInfo(body.info);
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                body.wrtr.name("more").value(settingUser.getInfo());
            }
            body.wrtr.endObject();
        } catch (Exception e) { body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chInfo", ans, TypesConnect.PROFILES, "main", "main", "main", user.getLogin());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chLogin")
    public JsonObject chLogin(@RequestBody DataProfile body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        if(ObjectUtils.isEmpty(body.oLogin)) {
            body.oLogin = subscriber.getLogin();
        }
        User user = datas.getDbService().userByLogin(body.oLogin);
        User userN = datas.getDbService().userByLogin(body.nLogin);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && userN == null && Objects.equals(body.oLogin, subscriber.getLogin())) {
                user.setLogin(body.nLogin);
                subscriber.setLogin(body.nLogin);
                datas.getDbService().getUserRepository().saveAndFlush(user);
                body.wrtr.name("login").value(user.getLogin());
            }
            body.wrtr.endObject();
        } catch (Exception e) { body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chLogin", ans, TypesConnect.PROFILES, "main", "main", "main", body.oLogin);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getProfile")
    public JsonObject getProfile(@RequestBody DataProfile body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        if(ObjectUtils.isEmpty(body.login)) {
            body.login = subscriber.getLogin();
        }
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null) {
                SettingUser settingUser = user.getSettings();
                body.wrtr.name("login").value(user.getLogin())
                    .name("ico").value(settingUser.getIco())
                    .name("id").value(user.getId());
                if (!ObjectUtils.isEmpty(user.getFio())) {
                    body.wrtr.name("fio").value(user.getFio());
                }
                if (!ObjectUtils.isEmpty(settingUser.getInfo())) {
                    body.wrtr.name("more").value(settingUser.getInfo());
                }
                body.wrtr.name("roles").beginObject();
                for (long i = 0; i < 5; i++) {
                    if (!user.getRoles().containsKey(i)) continue;
                    body.wrtr.name(i + "").beginObject();
                    Role role = user.getRoles().get(i);
                    if (!ObjectUtils.isEmpty(role.getEmail())) {
                        body.wrtr.name("email").value(role.getEmail());
                    }
                    if (!ObjectUtils.isEmpty(role.getYO())) {
                        School school = role.getYO();
                        if (school != null) {
                            body.wrtr.name("yo").value(school.getName());
                        }
                    }
                    if (!ObjectUtils.isEmpty(role.getGrp())) {
                        Group group = role.getGrp();
                        if (group != null) {
                            body.wrtr.name("group").value(group.getName());
                        }
                    }
                    if (!ObjectUtils.isEmpty(role.getSubjects())) {
                        body.wrtr.name("lessons").beginArray();
                        for (String name : role.getSubjects()) {
                            body.wrtr.value(name);
                        }
                        body.wrtr.endArray();
                    }
                    if(!ObjectUtils.isEmpty(role.getKids())) {
                        body.wrtr.name("kids").beginObject();
                        datas.usersByList(role.getKids(), false, body.wrtr);
                        body.wrtr.endObject();
                    }
                    if(!ObjectUtils.isEmpty(role.getParents())) {
                        body.wrtr.name("parents").beginObject();
                        datas.usersByList(role.getParents(), false, body.wrtr);
                        body.wrtr.endObject();
                    }
                    body.wrtr.endObject();
                }
                body.wrtr.endObject();
            }
            body.wrtr.endObject();
        } catch (Exception e) { body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.PROFILES, "main", "main", "main", body.login);
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataProfile {
    public String login, nLogin, oLogin, info, email, uuid;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}