package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.Main;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static ru.Main.datas;

/** RU: Контроллер для раздела авторизации
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AuthController">http://localhost:9001/swagger/htmlSwag/#/AuthController</a>
 * </pre>
 * @see Subscriber */
@Slf4j
@RequestMapping("/auth")
@NoArgsConstructor
@RestController public class AuthController {

    /** RU: [start] изменение подписки
     * @see DocsHelpController#point(Object, Object) Описание */
    @PatchMapping("/infCon")
    public ResponseEntity<JsonObject> infCon(@RequestBody DataAuth body, @AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /infCon");
        final User user = sub.getUser();
        SSEController.changeSubscriber(auth.getUUID(), body.login, body.type, null, null, null, null);
        if(user == null) return ResponseEntity.ok().build();

        if (!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            if(body.permis && !settingUser.getTokens().contains(body.notifToken)) {
                datas.getPushService().addToken(settingUser, body.notifToken);
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            }
            if(!body.permis && settingUser.getTokens().contains(body.notifToken)){
                datas.getPushService().remToken(settingUser, body.notifToken);
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            }
        }
        wrtr.name("role").value(user.getSelRole().i);
        if (user.getSelRole() == Roles.PARENT) {
            final Role role = user.getRoles().get(Roles.PARENT);
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

    /** RU: завершение сеанса
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub != null)")
    @PatchMapping("/remCon")
    public ResponseEntity<Void> remCon(@AuthenticationPrincipal Subscriber sub, CustomToken auth) {
        log.info("[PATCH] /remCon");
        if(sub.getLogin() != null) {
            log.debug("subscription remCon " + auth.getUUID() + " was noclosed " + sub.getLogin());
        } else {
            datas.subscriptions.remove(UUID.fromString(auth.getUUID()));
            log.debug("subscription remCon " + auth.getUUID() + " was closed");
        }
        sub.getSSE().complete();
        return ResponseEntity.ok().build();
    }

    /** RU: авторизация пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PostMapping("/auth")
    public ResponseEntity<JsonObject> auth(@RequestBody DataAuth body, @AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /auth");
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            if(body.permis) {
                datas.getPushService().addToken(settingUser, body.notifToken);
            } else {
                datas.getPushService().remToken(settingUser, body.notifToken);
            }
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
        }
        wrtr.name("auth").value(true)
            .name("login").value(user.getUsername())
            .name("role").value(user.getSelRole().i)
            .name("uuidS").value(auth.getUUID())
            .name("ico").value(user.getSettings().getIco())
            .name("roles").value(!ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1)
            .name("secFr").value(!ObjectUtils.isEmpty(user.getSettings().getSecFr()))
            .name("email").value(!ObjectUtils.isEmpty(user.getSettings().getEmail()));
        if(user.getSelRole() == Roles.PARENT) {
            final Role role = user.getRoles().get(Roles.PARENT);
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

    /** RU: регистрация пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/reg")
    public ResponseEntity<JsonObject> reg(@RequestBody DataAuth body) throws Exception {
        final User user = datas.getDbService().userByLogin(body.login),
            user1 = datas.getDbService().userByCode(body.code);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /reg");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user1 == null){
            wrtr.name("error").value("noInv");
            stat = HttpStatus.ACCEPTED;
        } else if(user == null) {
            if(Objects.equals(body.mod, "inv")) {
                if(user1.getSettings() == null) {
                    user1.setSettings(datas.getDbService().createSettingUser(new SettingUser(body.ico)));
                }
                user1.setSelRole(datas.getDbService().getFirstRoleId(user1.getRoles()));
                if(user1.getRoles().containsKey(Roles.PARENT) && !ObjectUtils.isEmpty(user1.getRoles().get(Roles.PARENT).getKids())) {
                    user1.setSelKid(user1.getRoles().get(Roles.PARENT).getKids().get(0).getId());
                }
            } else if(Objects.equals(body.mod, "rea")){
                user1.setCode(null);
                user1.setExpDate(null);
            }
            user1.setUsername(body.login);
            user1.setPassword(body.par);
            user1.getSettings().setIco(body.ico);
            datas.getDbService().getUserRepository().saveAndFlush(user1);
            if(user1.getSettings() != null) {
                final School school = datas.getDbService().getFirstRole(user1.getRoles()).getYO();
                if (school != null) {
                    datas.getPushService().addTopic(user1.getSettings(), school.getId() + "News");
                }
                datas.getPushService().addTopic(user1.getSettings(), "news");
                datas.getDbService().getSettingUserRepository().saveAndFlush(user1.getSettings());
                if(!ObjectUtils.isEmpty(body.secFr)) {
                    user1.getSettings().setSecFr(body.secFr);
                }
            }
            stat = HttpStatus.CREATED;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: проверка инвайта для регистрации/регистрации новой роли
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/checkInvCode")
    public ResponseEntity<Void> checkInvCode(@RequestBody DataAuth body) {
        final User user = datas.getDbService().userByCode(body.code);
        log.info("[POST] /checkInvCode ! " + body);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/setCodePep")
    public ResponseEntity<JsonObject> setCodePep(@RequestBody DataAuth body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user1 = datas.getDbService().userByLogin(body.id);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /setCodePep");
        if(user1 == null) return ResponseEntity.notFound().build();

        final UUID uuid = UUID.randomUUID();
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        user1.setCode(uuid.toString());
        user1.setExpDate(Main.df.format(dateAfter));
        datas.getDbService().getUserRepository().saveAndFlush(user1);
        final Long schId = datas.getDbService().getFirstRole(user1.getRoles()).getYO().getId();

        wrtr.name("id").value(user1.getId());
        log.debug("setCode " + uuid);

        wrtr.name("code").value(uuid.toString())
            .name("id1").value(schId);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("codPepL2C", ans, sub.getType(), "null", sub.getLvlGr(), "adm", "main");
            SSEController.sendEventFor("codPepL1C", ans, sub.getType(), schId +"", sub.getLvlGr(), "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые AuthController в методах
     * @see AuthController */
    @ToString
    @RequiredArgsConstructor
    static final class DataAuth {
        public final TypesConnect type;
        public final String code, notifToken, login, secFr, par, mod, id;
        public final int ico;
        public final boolean permis;
    }
}