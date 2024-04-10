package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.security.CustomToken;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static ru.mirea.Main.datas;

/** RU: Контроллер для авторизации и управлением Server Sent Events
 * <pre>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *
 *  toDo: Доделать
 *   + Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/auth")
@NoArgsConstructor
@RestController public class AuthController {

    /** RU: открытие Server Sent Events для нового клиента */
    @GetMapping(path = "/open-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter openSseStream() throws IOException {
        System.out.println("YT3 " + SecurityContextHolder.getContext().getAuthentication());
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        UUID uuid = UUID.randomUUID();
        Subscriber subscriber = new Subscriber(emitter);
        datas.subscriptions.put(uuid, subscriber);
        System.out.println("create subscription for " + uuid);
        emitter.send(SseEmitter.event()
            .name("chck").data(uuid));
        subscriber.setSSE(emitter, uuid);
        return emitter;
    }

    /** RU: пинг раз в 10 секунд*/
    @Scheduled(cron = "*/10 * * * * *")
    public void ping(){
        sendMessageFor("ping", "test", TypesConnect.MAIN, "main", "main", "main", "main");
    }

    /** RU: отправляет всем подходящим клиентам информацию по определённому ивенту
     * @param evName Название ивента
     * @param data Обычно JsonObject или текст
     * @param type Обозначает название раздела в котором находится клиент
     * @param lvlGr "main" обозначает любое значение
     * @see Subscriber*/
    public void sendMessageFor(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        var event = SseEmitter.event().name(evName).data(data);
        datas.subscriptions.forEach((uuid, subscriber) -> {
            if ((type == TypesConnect.MAIN || Objects.equals(type, subscriber.getType()))
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
        if(uuid != null) {
            Subscriber sub = datas.subscriptions.get(UUID.fromString(uuid));
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
    }

    /** RU: изменение подписки, также если даётся свойство uuid, то происходит пересмотр подписки
     * @param body Данные с клиента, задействуются свойства: type, uuid, notifToken, permis, login
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping(value = "/infCon")
    public ResponseEntity<JsonObject> infCon(@RequestBody DataAuth body, CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /infCon");
        TypesConnect type = null;
        if(!ObjectUtils.isEmpty(body.type)) {
            type = TypesConnect.valueOf(body.type);
        }
        String uuid = body.uuid;
        if(!ObjectUtils.isEmpty(uuid)) {
            Subscriber subscriber = getSubscriber(uuid);
            if (auth.getUUID() != null) {
                if (!Objects.equals(body.uuid, auth.getUUID())) {
                    if (auth.getSub().getLogin() != null) {
                        datas.subscriptions.remove(UUID.fromString(body.uuid));
                        System.out.println("subscriptionL " + uuid + " change to " + auth.getUUID());
                        uuid = auth.getUUID();
                        auth.getSub().setSSE(subscriber.getSSE(), UUID.fromString(uuid));
                        subscriber = auth.getSub();
                    } else {
                        datas.subscriptions.remove(UUID.fromString(auth.getUUID()));
                        wrtr.name("uuid").value(uuid);
                        System.out.println("subscriptionNL " + auth.getUUID() + " change to " + uuid);
                    }
                }
            } else {
                //body.uuid main
                wrtr.name("uuid").value(uuid);
                System.out.println("subscription null change to " + uuid);
            }
            User user = subscriber.getUser();
            if(user != null) {
                System.out.println(user);
                if (!ObjectUtils.isEmpty(body.notifToken)) {
                    SettingUser settingUser = user.getSettings();
                    if(body.permis && !settingUser.getTokens().contains(body.notifToken)) {
                        datas.getPushService().addToken(settingUser, body.notifToken);
                        datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                    }
                    if(!body.permis && settingUser.getTokens().contains(body.notifToken)){
                        datas.getPushService().remToken(settingUser, body.notifToken);
                        datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                    }
                }
                wrtr.name("role").value(user.getSelRole());
                if (user.getSelRole() == 1L) {
                    Role role = user.getRoles().get(1L);
                    wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (User kid : role.getKids()) {
                            wrtr.name(kid.getId() + "").value(kid.getFio());
                        }
                    }
                    wrtr.endObject();
                }
            }
        } else {
            uuid = auth.getUUID();
        }
        infCon(uuid, body.login, type, null, null, null, null);
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: завершение сеанса
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping(value = "/remCon")
    public ResponseEntity<Void> remCon(CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init("", "[PATCH] /remCon");
        if(auth.getSub().getLogin() != null) {
            System.out.println("subscription remCon " + auth.getUUID() + " was noclosed " + auth.getSub().getLogin());
        } else {
            datas.subscriptions.remove(UUID.fromString(auth.getUUID()));
            System.out.println("subscription remCon " + auth.getUUID() + " was closed");
        }
        auth.getSub().getSSE().complete();
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK);
    }

    /** RU: авторизация пользователя
     * @param body Данные с клиента, задействуются свойства: notifToken, permis, login, password
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PostMapping(value = "/auth")
    public ResponseEntity<JsonObject> auth(@RequestBody DataAuth body, CustomToken auth) throws Exception {
        User user = datas.getDbService().userByLogin(body.login);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /auth");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && Objects.equals(user.getPassword(), body.password)) {
            if(!ObjectUtils.isEmpty(body.notifToken)) {
                SettingUser settingUser = user.getSettings();
                if(body.permis) {
                    datas.getPushService().addToken(settingUser, body.notifToken);
                } else {
                    datas.getPushService().remToken(settingUser, body.notifToken);
                }
                datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            }

            wrtr.name("auth").value(true)
                .name("login").value(user.getUsername())
//                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : ((Long) user.getRoles().keySet().toArray()[4]));
                .name("role").value(user.getSelRole())
                .name("uuidS").value(auth.getUUID())
                .name("ico").value(user.getSettings().getIco())
                .name("roles").value(!ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1)
                .name("secFr").value(!ObjectUtils.isEmpty(user.getSettings().getSecFr()))
                .name("email").value(!ObjectUtils.isEmpty(user.getSettings().getEmail()));
            if(user.getSelRole() == 1L) {
                Role role = user.getRoles().get(1L);
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
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: регистрация пользователя
     * @param body Данные с клиента, задействуются свойства: secFr, ico, par, mod, login, code
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PostMapping(value = "/reg")
    public ResponseEntity<JsonObject> reg(@RequestBody DataAuth body) throws Exception {
        User user = datas.getDbService().userByLogin(body.login),
            user1 = datas.getDbService().userByCode(body.code);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /reg");
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
                if(user1.getRoles().containsKey(1L)) {
                    if(!ObjectUtils.isEmpty(user1.getRoles().get(1L).getKids())) {
                        user1.setSelKid(user1.getRoles().get(1L).getKids().get(0).getId());
                    }
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
                School school = datas.getDbService().getFirstRole(user1.getRoles()).getYO();
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
     * @param body Данные с клиента, задействуются свойства: code
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping(value = "/checkInvCode")
    public ResponseEntity<Void> checkInvCode(@RequestBody DataAuth body) throws Exception {
        User user = datas.getDbService().userByCode(body.code);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /checkInvCode");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null) stat = HttpStatus.OK;
        return datas.getObjR(ans -> {}, wrtr, stat);
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping(value = "/setCodePep")
    public ResponseEntity<JsonObject> setCodePep(@RequestBody DataAuth body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        User user1 = datas.getDbService().userByLogin(body.id);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /setCodePep");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            Long schId = null;
        };
        if(user != null && user1 != null
                && (user.getSelRole() == 3L && user.getRoles().containsKey(3L)
                || user.getSelRole() == 4L && user.getRoles().containsKey(4L))) {
            UUID uuid = UUID.randomUUID();
            Instant after = Instant.now().plus(Duration.ofDays(30));
            Date dateAfter = Date.from(after);
            user1.setCode(uuid.toString());
            user1.setExpDate(Main.df.format(dateAfter));
            datas.getDbService().getUserRepository().saveAndFlush(user1);
            ref.schId = datas.getDbService().getFirstRole(user1.getRoles()).getYO().getId();

            wrtr.name("id").value(user1.getId());
            System.out.println("setCode " + uuid);

            wrtr.name("code").value(uuid.toString())
                .name("id1").value(ref.schId);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            sendMessageFor("codPepL2C", ans, auth.getSub().getType(), "null", auth.getSub().getLvlGr(), "adm", "main");
            sendMessageFor("codPepL1C", ans, auth.getSub().getType(), ref.schId +"", auth.getSub().getLvlGr(), "ht", "main");
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые AuthController в методах
     * @see AuthController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataAuth {
        public String code, notifToken, login, secFr, par, mod,
            password, type, id, uuid;
        public int ico;
        public boolean permis;
    }
}