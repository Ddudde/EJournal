package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/** RU: Контроллер для раздела авторизации
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AuthController">http://localhost:9001/swagger/htmlSwag/#/AuthController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final SettingUserRepository settingUserRepository;
    private final DBService dbService;
    private final MainService mainService;
    private final PushService pushService;
    private final UserRepository userRepository;

    /** RU: [start] изменение подписки
     * @see DocsHelpController#point(Object, Object) Описание */
    @PatchMapping("/infCon")
    public ResponseEntity<JsonObject> infCon(@RequestBody DataAuth body, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /infCon");
        final User user = dbService.userByLogin(body.login);
        SSEController.changeSubscriber(auth.getUUID(), body.login, body.type, null, null, null, null);
        if(user == null) return ResponseEntity.ok().build();

        sub.setUserId(user.getId());
        if (!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            if(body.permis && !settingUser.getTokens().contains(body.notifToken)) {
                pushService.addToken(settingUser, body.notifToken);
                settingUserRepository.saveAndFlush(settingUser);
            }
            if(!body.permis && settingUser.getTokens().contains(body.notifToken)){
                pushService.remToken(settingUser, body.notifToken);
                settingUserRepository.saveAndFlush(settingUser);
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
        return mainService.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: завершение сеанса
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub != null)")
    @PatchMapping("/remCon")
    public ResponseEntity<Void> remCon(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        log.info("[PATCH] /remCon");
        if(sub.getLogin() != null) {
            log.debug("subscription remCon " + auth.getUUID() + " was noclosed " + sub.getLogin());
        } else {
            MainService.subscriptions.remove(UUID.fromString(auth.getUUID()));
            log.debug("subscription remCon " + auth.getUUID() + " was closed");
        }
        sub.getSSE().complete();
        return ResponseEntity.ok().build();
    }

    /** RU: авторизация пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/auth")
    public ResponseEntity<JsonObject> auth(@RequestBody DataAuth body, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /auth");
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            if(body.permis) {
                pushService.addToken(settingUser, body.notifToken);
            } else {
                pushService.remToken(settingUser, body.notifToken);
            }
            settingUserRepository.saveAndFlush(settingUser);
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
        return mainService.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: регистрация пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/reg")
    public ResponseEntity<JsonObject> reg(@RequestBody DataAuth body) throws Exception {
        final User user = dbService.userByLogin(body.login),
            user1 = dbService.userByCode(body.code);
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /reg");
        final HttpStatus stat = createUser(wrtr, user, user1, body);
        return mainService.getObjR(ans -> {}, wrtr, stat, false);
    }

    private HttpStatus createUser(JsonTreeWriter wrtr, User existLogin, User invitedUser, DataAuth body) throws IOException {
        if(invitedUser == null) {
            wrtr.name("error").value("noInv");
            return HttpStatus.ACCEPTED;
        }
        if(existLogin != null) return HttpStatus.NOT_FOUND;

        if(Objects.equals(body.mod, "inv")) {
            if(invitedUser.getSettings() == null) {
                invitedUser.setSettings(dbService.createSettingUser(new SettingUser(body.ico)));
            }
            invitedUser.setSelRole(dbService.getFirstRoleId(invitedUser.getRoles()));
            if(invitedUser.getRoles().containsKey(Roles.PARENT) && !ObjectUtils.isEmpty(invitedUser.getRole(Roles.PARENT).getKids())) {
                invitedUser.setSelKid(invitedUser.getRole(Roles.PARENT).getKids().get(0).getId());
            }
        } else if(Objects.equals(body.mod, "rea")){
            invitedUser.setCode(null);
            invitedUser.setExpDate(null);
        }
        invitedUser.setUsername(body.login);
        invitedUser.setPassword(passwordEncoder.encode(body.par));
        invitedUser.getSettings().setIco(body.ico);
        userRepository.saveAndFlush(invitedUser);
        if(invitedUser.getSettings() != null) {
            final School school = dbService.getFirstRole(invitedUser.getRoles()).getYO();
            if (school != null) {
                pushService.addTopic(invitedUser.getSettings(), school.getId() + "News");
            }
            pushService.addTopic(invitedUser.getSettings(), "news");
            settingUserRepository.saveAndFlush(invitedUser.getSettings());
            if(!ObjectUtils.isEmpty(body.secFr)) {
                invitedUser.getSettings().setSecFr(body.secFr);
            }
        }
        return HttpStatus.CREATED;
    }

    /** RU: проверка инвайта для регистрации/регистрации новой роли
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/checkInvCode")
    public ResponseEntity<Void> checkInvCode(@RequestBody DataAuth body) {
        final User user = dbService.userByCode(body.code);
        log.info("[POST] /checkInvCode ! " + body);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/setCodePep")
    public ResponseEntity<JsonObject> setCodePep(@RequestBody DataAuth body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user1 = dbService.userByLogin(body.id);
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /setCodePep");
        if(user1 == null) return ResponseEntity.notFound().build();

        final UUID uuid = UUID.randomUUID();
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        user1.setCode(uuid.toString());
        user1.setExpDate(MainService.df.format(dateAfter));
        userRepository.saveAndFlush(user1);
        final Long schId = dbService.getFirstRole(user1.getRoles()).getYO().getId();

        wrtr.name("id").value(user1.getId());
        log.debug("setCode " + uuid);

        wrtr.name("code").value(uuid.toString())
            .name("id1").value(schId);
        return mainService.getObjR(ans -> {
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