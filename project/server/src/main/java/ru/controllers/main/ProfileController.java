package ru.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.Role;
import ru.data.models.auth.SettingUser;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import static ru.Main.datas;

/** RU: Контроллер для раздела профиля и частично управлением аккаунтом + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/ProfileController">http://localhost:9001/swagger/htmlSwag/#/ProfileController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/profiles")
@RequiredArgsConstructor
@RestController public class ProfileController {

    private final AuthController authController;

    /** RU: изменение контроллируемого ученика у родителя
     * @param body Данные с клиента, задействуются свойства: idL
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping("/chKid")
    public ResponseEntity<JsonObject> chKid(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chKid");
        if(user != null && user.getSelRole() == Roles.PARENT && body.idL != null) {
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
    @PatchMapping("/chRole")
    public ResponseEntity<JsonObject> chRole(final CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final JsonTreeWriter wrtr = datas.init("", "[PATCH] /chRole");
        if(user != null && user.getRoles().containsKey(user.getSelRole())) {
            final Roles curRol = user.getSelRole();
            int i = curRol.i+1;
            Roles roleI;
            while(true){
                if(i == 5) i = 0;
                roleI = Roles.roleByI(i);
                if(!user.getRoles().containsKey(roleI)) {
                    i++;
                    continue;
                }
                break;
            }
            wrtr.name("role").value(i);
            user.setSelRole(roleI);
            datas.getDbService().getUserRepository().saveAndFlush(user);
            Role role = user.getRoles().get(roleI);
            if(roleI == Roles.PARENT) {
                wrtr.name("kid").value(user.getSelKid())
                    .name("kids").beginObject();
                if (!ObjectUtils.isEmpty(role.getKids())) {
                    for (User kid : role.getKids()) {
                        wrtr.name(kid.getId() + "").value(kid.getFio());
                    }
                }
                wrtr.endObject();
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
    @PatchMapping("/exit")
    public ResponseEntity<Void> exit(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        System.out.println("[PATCH] /exit ! " + body.toString());
        HttpStatus stat = HttpStatus.NOT_FOUND;
        User user = auth.getSub().getUser();
        if(user != null) {
            if (!ObjectUtils.isEmpty(body.notifToken)) {
                SettingUser settingUser = user.getSettings();
                datas.getPushService().remToken(settingUser, body.notifToken);
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            }
            stat = HttpStatus.OK;
        }
        auth.getSub().setLogin(null);
        auth.getSub().setLvlSch(null);
        auth.getSub().setLvlGr(null);
        return ResponseEntity.status(stat).build();
    }

    /** RU: изменение/добавление электронной почты определённой роли пользователя + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: email
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chEmail")
    public ResponseEntity<Void> chEmail(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chEmail");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null) {
            user.getSelecRole().setEmail(body.email);
            datas.getDbService().getUserRepository().saveAndFlush(user);
            wrtr.name("body").beginObject()
                .name("email").value(body.email)
                .name("role").value(user.getSelRole().i)
                .endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chEmail", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, stat);
    }

    /** RU: изменение/добавление дополнительной информации о пользователе + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: info
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chInfo")
    public ResponseEntity<Void> chInfo(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chInfo");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null) {
            SettingUser settingUser = user.getSettings();
            settingUser.setInfo(body.info);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            wrtr.name("body").beginObject()
                .name("more").value(settingUser.getInfo())
                .endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chInfo", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, stat);
    }

    /** RU: изменение логина пользователя + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: nLogin
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chLogin")
    public ResponseEntity<Void> chLogin(@RequestBody DataProfile body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        User userN = datas.getDbService().userByLogin(body.nLogin);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chLogin");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && userN == null) {
            user.setUsername(body.nLogin);
            auth.getSub().setLogin(body.nLogin);
            datas.getDbService().getUserRepository().saveAndFlush(user);
            wrtr.name("body").beginObject()
                .name("oLogin").value(auth.getSub().getLvlMore2())
                .name("nLogin").value(user.getUsername())
                .endObject();
            auth.getSub().setLvlMore2(body.nLogin);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chLogin", ans, TypesConnect.PROFILES, "main", "main", "main", body.nLogin);
        }, wrtr, stat);
    }

    /** RU: [start] отправляет инфу профиля либо другого пользователя либо личную
     * @param login Логин пользователя
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping({"/getProfile", "/getProfile/{login}"})
    public ResponseEntity<JsonObject> getProfile(@PathVariable(required = false) String login, CustomToken auth) throws Exception {
        User user;
        if(ObjectUtils.isEmpty(login)) {
            user = auth.getSub().getUser();
        } else {
            user = datas.getDbService().userByLogin(login);
        }
        JsonTreeWriter wrtr = datas.init(login, "[GET] /getProfile");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null) {
            SettingUser settingUser = user.getSettings();
            wrtr.name("login").value(user.getUsername())
                .name("ico").value(settingUser.getIco())
                .name("id").value(user.getId());
            if (!ObjectUtils.isEmpty(user.getFio())) {
                wrtr.name("fio").value(user.getFio());
            }
            if (!ObjectUtils.isEmpty(settingUser.getInfo())) {
                wrtr.name("more").value(settingUser.getInfo());
            }
            wrtr.name("roles").beginObject();
            for (int i = 0; i < 5; i++) {
                Roles roleI = Roles.roleByI(i);
                if (!user.getRoles().containsKey(roleI)) continue;
                wrtr.name(i + "").beginObject();
                Role role = user.getRoles().get(roleI);
                if (!ObjectUtils.isEmpty(role.getEmail())) {
                    wrtr.name("email").value(role.getEmail());
                }
                if (!ObjectUtils.isEmpty(role.getYO())) {
                    School school = role.getYO();
                    if (school != null) {
                        wrtr.name("yo").value(school.getName());
                    }
                }
                if (!ObjectUtils.isEmpty(role.getGrp())) {
                    Group group = role.getGrp();
                    if (group != null) {
                        wrtr.name("group").value(group.getName());
                    }
                }
                if (!ObjectUtils.isEmpty(role.getSubjects())) {
                    wrtr.name("lessons").beginArray();
                    for (String name : role.getSubjects()) {
                        wrtr.value(name);
                    }
                    wrtr.endArray();
                }
                if(!ObjectUtils.isEmpty(role.getKids())) {
                    wrtr.name("kids").beginObject();
                    datas.usersByList(role.getKids(), false, wrtr);
                    wrtr.endObject();
                }
                if(!ObjectUtils.isEmpty(role.getParents())) {
                    wrtr.name("parents").beginObject();
                    datas.usersByList(role.getParents(), false, wrtr);
                    wrtr.endObject();
                }
                wrtr.endObject();
            }
            wrtr.endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), auth.getSub().getLogin(), TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые ProfileController в методах
     * @see ProfileController */
    @ToString
    @RequiredArgsConstructor
    static class DataProfile {
        public final String nLogin, info, email, notifToken;
        public final Long idL;
    }
}