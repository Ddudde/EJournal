package ru.mirea.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.security.CustomToken;

import java.util.Objects;

import static ru.mirea.Main.datas;

@RequestMapping("/profiles")
@NoArgsConstructor
@RestController public class ProfileController {

    @Autowired
    private AuthController authController;

    /** RU: изменение контроллируемого ученика у родителя
     * @param body Данные с клиента, задействуются свойства: idL
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping(value = "/chKid")
    public ResponseEntity<JsonObject> chKid(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chKid");
        if(user != null && user.getSelRole() == 1L && body.idL != null) {
            user.setSelKid(body.idL);
            wrtr.name("kid").value(user.getSelKid());
            datas.getDbService().getUserRepository().saveAndFlush(user);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: изменение роли на следующую по иерархии из имеющихся у пользователя
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping(value = "/chRole")
    public ResponseEntity<JsonObject> chRole(CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        long curRol = user.getSelRole();
        JsonTreeWriter wrtr = datas.init("", "[PATCH] /chRole");
        if(user != null && user.getRoles().containsKey(curRol)) {
            for(long i = (curRol == 4 ? 0 : curRol+1L); i < 5; i++){
                if(!user.getRoles().containsKey(i)) continue;
                wrtr.name("role").value(i);
                user.setSelRole(i);
                datas.getDbService().getUserRepository().saveAndFlush(user);
                Role role = user.getRoles().get(i);
                if(i == 1L) {
                    wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (User kid : role.getKids()) {
                            wrtr.name(kid.getId() + "").value(kid.getFio());
                        }
                    }
                    wrtr.endObject();
                }
                break;
            }
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: выход с аккаунта
     * @param body Данные с клиента, задействуются свойства: notifToken
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping(value = "/exit")
    public ResponseEntity<JsonObject> exit(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /exit");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (!ObjectUtils.isEmpty(body.notifToken)) {
            User user = auth.getSub().getUser();
            if(user != null) {
                SettingUser settingUser = user.getSettings();
                datas.getPushService().remToken(settingUser, body.notifToken);
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                stat = HttpStatus.OK;
            }
        }
        auth.getSub().setLogin(null);
        auth.getSub().setLvlSch(null);
        auth.getSub().setLvlGr(null);
        return datas.getObjR(ans -> {}, wrtr, stat);
    }

    @PostMapping(value = "/chEmail")
    public JsonObject chEmail(@RequestBody DataProfile body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        if(ObjectUtils.isEmpty(body.login)) {
            body.login = subscriber.getLogin();
        }
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.init(body.toString());
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
            authController.sendMessageFor("chEmail", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
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
            body.wrtr = datas.init(body.toString());
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
            authController.sendMessageFor("chInfo", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
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
            body.wrtr = datas.init(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null && userN == null && Objects.equals(body.oLogin, subscriber.getLogin())) {
                user.setUsername(body.nLogin);
                subscriber.setLogin(body.nLogin);
                datas.getDbService().getUserRepository().saveAndFlush(user);
                body.wrtr.name("login").value(user.getUsername());
            }
            body.wrtr.endObject();
        } catch (Exception e) { body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("chLogin", ans, TypesConnect.PROFILES, "main", "main", "main", body.oLogin);
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
            body.wrtr = datas.init(body.toString());
            body.wrtr.name("body").beginObject();
            if (user != null) {
                SettingUser settingUser = user.getSettings();
                body.wrtr.name("login").value(user.getUsername())
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
    public String login, nLogin, oLogin, info, email, uuid, notifToken;
    public Long idL;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}