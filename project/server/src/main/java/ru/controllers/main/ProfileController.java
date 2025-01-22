package ru.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import static ru.Main.datas;

/** RU: Контроллер для раздела профиля и частично управлением аккаунтом + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ProfileController">http://localhost:9001/swagger/htmlSwag/#/ProfileController</a>
 * </pre>
 * @see Subscriber */
@Slf4j
@RequestMapping("/profiles")
@RequiredArgsConstructor
@RestController public class ProfileController {

    /** RU: изменение контроллируемого ученика у родителя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('PARENT')""")
    @PatchMapping("/chKid")
    public ResponseEntity<JsonObject> chKid(@RequestBody DataProfile body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chKid");
        if(body.idL == null) return ResponseEntity.notFound().build();

        user.setSelKid(body.idL);
        wrtr.name("kid").value(user.getSelKid());
        datas.getDbService().getUserRepository().saveAndFlush(user);
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: изменение роли на следующую по иерархии из имеющихся у пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PatchMapping("/chRole")
    public ResponseEntity<JsonObject> chRole(final @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[PATCH] /chRole");
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
        final Role role = user.getRoles().get(roleI);
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
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: выход с аккаунта
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PatchMapping("/exit")
    public ResponseEntity<Void> exit(@RequestBody DataProfile body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        log.info("[PATCH] /exit ! " + body);
        final User user = sub.getUser();
        if (!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            datas.getPushService().remToken(settingUser, body.notifToken);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
        }
        sub.setLogin(null);
        sub.setLvlSch(null);
        sub.setLvlGr(null);
        return ResponseEntity.ok().build();
    }

    /** RU: изменение/добавление электронной почты определённой роли пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PatchMapping("/chEmail")
    public ResponseEntity<Void> chEmail(@RequestBody DataProfile body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chEmail");
        user.getSelecRole().setEmail(body.email);
        datas.getDbService().getUserRepository().saveAndFlush(user);
        wrtr.name("body").beginObject()
            .name("email").value(body.email)
            .name("role").value(user.getSelRole().i)
            .endObject();
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chEmail", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение/добавление дополнительной информации о пользователе + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PatchMapping("/chInfo")
    public ResponseEntity<Void> chInfo(@RequestBody DataProfile body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chInfo");
        final SettingUser settingUser = user.getSettings();
        settingUser.setInfo(body.info);
        datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
        wrtr.name("body").beginObject()
            .name("more").value(settingUser.getInfo())
            .endObject();
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chInfo", ans, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение логина пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PatchMapping("/chLogin")
    public ResponseEntity<Void> chLogin(@RequestBody DataProfile body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final User userN = datas.getDbService().userByLogin(body.nLogin);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chLogin");
        if (userN != null) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        user.setUsername(body.nLogin);
        sub.setLogin(body.nLogin);
        datas.getDbService().getUserRepository().saveAndFlush(user);
        wrtr.name("body").beginObject()
            .name("oLogin").value(sub.getLvlMore2())
            .name("nLogin").value(user.getUsername())
            .endObject();
        sub.setLvlMore2(body.nLogin);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chLogin", ans, TypesConnect.PROFILES, "main", "main", "main", body.nLogin);
        }, wrtr, HttpStatus.OK);
    }

    /** RU: [start] отправляет инфу профиля либо другого пользователя либо личную
     * @see DocsHelpController#point(Object, Object) Описание */
    @GetMapping({"/getProfile", "/getProfile/{login}"})
    public ResponseEntity<JsonObject> getProfile(@PathVariable(required = false) String login, CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        User user;
        if(ObjectUtils.isEmpty(login)) {
            user = sub.getUser();
        } else {
            user = datas.getDbService().userByLogin(login);
        }
        final JsonTreeWriter wrtr = datas.init(login, "[GET] /getProfile");
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        final SettingUser settingUser = user.getSettings();
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
            final Roles roleI = Roles.roleByI(i);
            if (!user.getRoles().containsKey(roleI)) continue;
            wrtr.name(i + "").beginObject();
            final Role role = user.getRoles().get(roleI);
            if (!ObjectUtils.isEmpty(role.getEmail())) {
                wrtr.name("email").value(role.getEmail());
            }
            if (!ObjectUtils.isEmpty(role.getYO())) {
                final School school = role.getYO();
                if (school != null) {
                    wrtr.name("yo").value(school.getName());
                }
            }
            if (!ObjectUtils.isEmpty(role.getGrp())) {
                final Group group = role.getGrp();
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
        return datas.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), sub.getLogin(), TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые ProfileController в методах
     * @see ProfileController */
    @ToString
    @RequiredArgsConstructor
    static final class DataProfile {
        public final String nLogin, info, email, notifToken;
        public final Long idL;
    }
}