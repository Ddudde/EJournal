package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.Main;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.Role;
import ru.data.models.auth.SettingUser;
import ru.data.models.auth.User;
import ru.data.models.school.School;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static ru.Main.datas;

/** RU: Контроллер для раздела авторизации и управлением Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AuthController">http://localhost:9001/swagger/htmlSwag/#/AuthController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/auth")
@NoArgsConstructor
@RestController public class AuthController {

    /** RU: [start#1] открытие Server Sent Events для нового клиента
     * или сохранение подписки для старого пользователя
     * @param uuidAuth Авторизация, в ней подписка и пользователь
     * @exception IOException Исключение вызывается при ошибках с Json */
    @GetMapping(value = {"/start/{uuidAuth}", "/start"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter start(@PathVariable(required = false) String uuidAuth) throws IOException {
        System.out.println("YT3 " + SecurityContextHolder.getContext().getAuthentication());
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        UUID uuid;
        Subscriber subscriber;
        if (uuidAuth != null && !uuidAuth.equals("null")) {
            uuid = UUID.fromString(uuidAuth);
            subscriber = getSubscriber(uuidAuth);
            if (subscriber != null && subscriber.getLogin() != null) {
                System.out.println("subscriptionL save " + uuidAuth);
            } else {
                if (subscriber != null) {
                    datas.subscriptions.remove(uuid);
                }
                uuid = UUID.randomUUID();
                subscriber = new Subscriber(emitter);
                datas.subscriptions.put(uuid, subscriber);
                System.out.println("subscriptionNL change to " + uuid);
            }
        } else {
            uuid = UUID.randomUUID();
            subscriber = new Subscriber(emitter);
            datas.subscriptions.put(uuid, subscriber);
            System.out.println("create subscription for " + uuid);
        }
        subscriber.setSSE(emitter, uuid);
        emitter.send(SseEmitter.event().name("chck")
            .data(uuid));
        return emitter;
    }

    /** RU: отправляет всем подходящим клиентам информацию по определённому ивенту
     * @param evName Название ивента
     * @param data Обычно JsonObject или текст
     * @param type Обозначает название раздела в котором находится клиент
     * @param lvlGr "main" обозначает любое значение
     * @see Subscriber*/
    public void sendEventFor(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        var event = SseEmitter.event().name(evName).data(data);
        datas.subscriptions.forEach((uuid, subscriber) -> {
            if (!subscriber.isSSEComplete()
             && (type == TypesConnect.MAIN || Objects.equals(type, subscriber.getType()))
             && (Objects.equals(lvlSch, "main") || Objects.equals(lvlSch, subscriber.getLvlSch()))
             && (Objects.equals(lvlGr, "main") || Objects.equals(lvlGr, subscriber.getLvlGr()))
             && (Objects.equals(lvlMore1, "main") || Objects.equals(lvlMore1, subscriber.getLvlMore1()))
             && (Objects.equals(lvlMore2, "main") || Objects.equals(lvlMore2, subscriber.getLvlMore2()))) {
                try {
                    subscriber.getSSE().send(event);
                } catch (IOException e) {
                    if(subscriber.getLogin() == null) {
                        datas.subscriptions.remove(uuid);
                        System.out.println("subscription " + uuid + " was closed from Ping or Error");
                    } else {
                        System.out.println("subscription " + uuid + " was noclosed from Ping or Error " + subscriber.getLogin());
                    }
                    subscriber.getSSE().complete();
                }
            }
        });
    }

    public Subscriber getSubscriber(String uuid) {
        if(!ObjectUtils.isEmpty(uuid)) {
            return datas.subscriptions.get(UUID.fromString(uuid));
        }
        return null;
    }

    /** RU: изменение подписки
     * Все параметры, являются свойствами подписки*/
    public void infCon(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid == null) return;
        final Subscriber sub = datas.subscriptions.get(UUID.fromString(uuid));
        if(sub == null) return;
        if(login != null) {
            sub.setLogin(login);
            System.out.println("setLog " + login + " subscription for " + uuid);
        }
        if(type != null) {
            sub.setType(type);
            System.out.println("setType " + type + " subscription for " + uuid);
        }
        if(lvlSch != null) {
            sub.setLvlSch(lvlSch);
            System.out.println("setLvlSch " + lvlSch + " subscription for " + uuid);
        }
        if(lvlGr != null) {
            sub.setLvlGr(lvlGr);
            System.out.println("setLvlGr " + lvlGr + " subscription for " + uuid);
        }
        if(lvlMore1 != null) {
            sub.setLvlMore1(lvlMore1);
            System.out.println("setLvlMore1 " + lvlMore1 + " subscription for " + uuid);
        }
        if(lvlMore2 != null) {
            sub.setLvlMore2(lvlMore2);
            System.out.println("setLvlMore2 " + lvlMore2 + " subscription for " + uuid);
        }
    }

    /** RU: [start#2] изменение подписки
     * @see DocsHelpController#point(Object, Object) Описание */
    @PatchMapping("/infCon")
    public ResponseEntity<JsonObject> infCon(@RequestBody DataAuth body, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /infCon");
        final User user = auth.getSub().getUser();
        infCon(auth.getUUID(), body.login, body.type, null, null, null, null);
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
    @PatchMapping("/remCon")
    public ResponseEntity<Void> remCon(CustomToken auth) {
        System.out.println("[PATCH] /remCon");
        if(auth.getSub().getLogin() != null) {
            System.out.println("subscription remCon " + auth.getUUID() + " was noclosed " + auth.getSub().getLogin());
        } else {
            datas.subscriptions.remove(UUID.fromString(auth.getUUID()));
            System.out.println("subscription remCon " + auth.getUUID() + " was closed");
        }
        auth.getSub().getSSE().complete();
        return ResponseEntity.ok().build();
    }

    /** RU: авторизация пользователя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/auth")
    public ResponseEntity<JsonObject> auth(@RequestBody DataAuth body, CustomToken auth) throws Exception {
        final User user = datas.getDbService().userByLogin(body.login);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /auth");
        if(user == null || !Objects.equals(user.getPassword(), body.password)) {
            return ResponseEntity.notFound().build();
        }
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
        infCon(auth.getUUID(), body.login, null, null, null, null, null);
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
        System.out.println("[POST] /checkInvCode ! " + body);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/setCodePep")
    public ResponseEntity<JsonObject> setCodePep(@RequestBody DataAuth body, CustomToken auth) throws Exception {
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
        System.out.println("setCode " + uuid);

        wrtr.name("code").value(uuid.toString())
            .name("id1").value(schId);
        return datas.getObjR(ans -> {
            sendEventFor("codPepL2C", ans, auth.getSub().getType(), "null", auth.getSub().getLvlGr(), "adm", "main");
            sendEventFor("codPepL1C", ans, auth.getSub().getType(), schId +"", auth.getSub().getLvlGr(), "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые AuthController в методах
     * @see AuthController */
    @ToString
    @RequiredArgsConstructor
    static final class DataAuth {
        public final TypesConnect type;
        public final String code, notifToken, login, secFr, par, mod,
            password, id;
        public final int ico;
        public final boolean permis;
    }
}