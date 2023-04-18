package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import ru.mirea.Main;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.json.Role;
import ru.mirea.data.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000", "https://ddudde.github.io"})
public class AuthController {

    @Autowired
    private ServerService datas;

    Map<UUID, Subscriber> subscriptions = new ConcurrentHashMap<>();

    @GetMapping(path = "/open-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> openSseStream() {
        Flux<ServerSentEvent> stream = Flux.create(fluxSink -> {
            UUID uuid = UUID.randomUUID();
            Subscriber subscriber = new Subscriber(fluxSink);
            subscriptions.put(uuid, subscriber);
            System.out.println("create subscription for " + uuid);
            ServerSentEvent<Object> event = ServerSentEvent
                    .builder()
                    .event("chck")
                    .data(uuid)
                    .build();
            fluxSink.next(event);
            fluxSink.onCancel(
                () -> {
                    subscriptions.remove(uuid);
                    System.out.println("subscription " + uuid + " was closed from onCancel");
                }
            );
            fluxSink.onDispose(
                () -> {
                    subscriptions.remove(uuid);
                    System.out.println("subscription " + uuid + " was closed from onDispose");
                }
            );
            ServerSentEvent<Object> event1 = ServerSentEvent
                    .builder()
                    .event("connect")
                    .data("")
                    .build();
            fluxSink.next(event1);
        });
        return stream;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void ping(){
        sendMessageForAll("ping", "test", TypesConnect.MAIN, "main", "main", "main", "main");
    }

    public void sendMessageForAll(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        ServerSentEvent<Object> event = ServerSentEvent
                .builder()
                .event(evName)
                .data(data)
                .build();
        subscriptions.forEach((uuid, subscriber) -> {
                if ((type == TypesConnect.MAIN || Objects.equals(type, subscriber.getType()))
                 && (Objects.equals(lvlSch, "main") || Objects.equals(lvlSch, subscriber.getLvlSch()))
                 && (Objects.equals(lvlGr, "main") || Objects.equals(lvlGr, subscriber.getLvlGr()))
                 && (Objects.equals(lvlMore1, "main") || Objects.equals(lvlMore1, subscriber.getLvlMore1()))
                 && (Objects.equals(lvlMore2, "main") || Objects.equals(lvlMore2, subscriber.getLvlMore2()))) {
                    try {
                        subscriber.getFluxSink().next(event);
                    } catch (Error e) {
                        subscriptions.remove(uuid);
                        System.out.println("subscription " + uuid + " was closed from Ping");
                    }
                }
            }
        );
    }

    public Subscriber getSubscriber(String uuid) {
        return subscriptions.get(UUID.fromString(uuid));
    }

    public void infCon(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid != null) {
            if(login != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setLogin(login);
                System.out.println("setLog " + login + " subscription for " + uuid);
            }
            if(type != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setType(type);
                System.out.println("setType " + type + " subscription for " + uuid);
            }
            if(lvlSch != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setLvlSch(lvlSch);
                System.out.println("setLvlSch " + lvlSch + " subscription for " + uuid);
            }
            if(lvlGr != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setLvlGr(lvlGr);
                System.out.println("setLvlGr " + lvlGr + " subscription for " + uuid);
            }
            if(lvlMore1 != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setLvlMore1(lvlMore1);
                System.out.println("setLvlMore1 " + lvlMore1 + " subscription for " + uuid);
            }
            if(lvlMore2 != null) {
                subscriptions.get(UUID.fromString(uuid))
                        .setLvlMore2(lvlMore2);
                System.out.println("setLvlMore2 " + lvlMore2 + " subscription for " + uuid);
            }
        }
    }

    @PostMapping(value = "/infCon")
    public JsonObject infCon(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        try {
            wrtr.beginObject().name("error").value(false);
            String uuid = null, login = null;
            TypesConnect type = null;
            if(body.has("uuid")){
                uuid = body.get("uuid").getAsString();
            }
            if(body.has("login")){
                login = body.get("login").getAsString();
            }
            if(body.has("type")){
                type = TypesConnect.valueOf(body.get("type").getAsString());
            }
            infCon(uuid, login, type, null, null, null, null);
            wrtr.name("yes").value(true);
        } catch (Exception e) {
            wrtr.name("error").value(true);
//            System.out.println(e.fillInStackTrace());
            e.printStackTrace();
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/remCon")
    public JsonObject remCon(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        try {
            wrtr.beginObject().name("error").value(false);
            if(body.has("uuid")) {
                subscriptions.remove(UUID.fromString(body.get("uuid").getAsString()));
                System.out.println("subscription remCon " + body.get("uuid").getAsString() + " was closed");
            }
            wrtr.name("yes").value(true);
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/auth")
    public JsonObject auth(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = datas.userByLogin(body.get("login").getAsString());
        try {
            wrtr.beginObject().name("error").value(false)
                .name("body").beginObject();
            if(user != null) {
                if(Objects.equals(user.getPassword(), body.get("password").getAsString())) {
                    wrtr.name("auth").value(true)
                        .name("login").value(user.getLogin())
//                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : ((Long) user.getRoles().keySet().toArray()[4]));
                        .name("role").value(ObjectUtils.isEmpty(user.getRoles()) ? 0 : datas.getFirstRoleId(user.getRoles()))
                        .name("ico").value(user.getIco())
                        .name("roles").value(!ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1)
                        .name("secFr").value(!ObjectUtils.isEmpty(user.getSecFr()));
                    infCon(body.get("uuid").getAsString(), body.get("login").getAsString(), null, null, null, null, null);
                }
            }
            wrtr.endObject();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/reg")
    public JsonObject reg(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = datas.userByLogin(body.get("login").getAsString());
        Invite inv = null;
        User user1 = null;
        if(Objects.equals(body.get("mod").getAsString(), "inv")){
            inv = datas.inviteByCode(body.get("code").getAsString());
        }
        if(Objects.equals(body.get("mod").getAsString(), "rea")){
            user1 = datas.userByCode(body.get("code").getAsString());
        }
        try {
            wrtr.beginObject().name("error").value(false);
            if(inv == null && user1 == null){
                wrtr.name("error").value(2);
                wrtr.name("yes").value(false);
                return datas.getObj(ans -> {}, wrtr, true);
            }
            if(user == null) {
                if(inv != null) {
                    user = new User(body.get("login").getAsString(), body.get("par").getAsString(), body.get("ico").getAsInt());
                    user.setRoles(inv.getRole());
                    user.setFio(inv.getFio());
                    datas.getUserRepository().saveAndFlush(user);
                    School school = datas.schoolById(datas.getFirstRole(inv.getRole()).getYO());
                    if(ObjectUtils.isEmpty(school.getHteachersInv())) school.setHteachersInv(new ArrayList<>());
                    if(ObjectUtils.isEmpty(school.getHteachers())) school.setHteachers(new ArrayList<>());
                    school.getHteachersInv().remove(inv.getId());
                    school.getHteachers().add(user.getId());
                    datas.getSchoolRepository().saveAndFlush(school);
                    datas.getInviteRepository().delete(inv);
                }
                if(user1 != null){
                    user1.setLogin(body.get("login").getAsString());
                    user1.setPassword(body.get("par").getAsString());
                    user1.setIco(body.get("ico").getAsInt());
                    user1.setCode(null);
                    user1.setExpDate(null);
                    datas.getUserRepository().saveAndFlush(user1);
                }
            }
            wrtr.name("yes").value(false);
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/chPass")
    public JsonObject chPass(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = datas.userByLogin(body.get("login").getAsString());
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && Objects.equals(user.getSecFr(), body.get("secFr").getAsString())) {
                user.setPassword(body.get("par").getAsString());
                datas.getUserRepository().saveAndFlush(user);
                wrtr.name("yes").value(false);
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/chRole")
    public JsonObject chRole(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = datas.userByLogin(body.get("login").getAsString());
        long curRol = body.get("role").getAsLong();
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && user.getRoles().containsKey(curRol)) {
                wrtr.name("body").beginObject();
                for(long i = (curRol == 4 ? 0 : curRol+1L); i < 5; i++){
                    if(!user.getRoles().containsKey(i)) continue;
                    wrtr.name("role").value(i);
                    Role role = user.getRoles().get(i);
                    if(role.getKids() != null && !role.getKids().isEmpty()){
                        wrtr.name("kid").value(role.getKids().get(0))
                            .name("kids").beginObject();
                        for(Long i1 : role.getKids()){
                            User kid = datas.userById(i1);
                            wrtr.name(i1+"").value(kid.getFio());
                        }
                        wrtr.endObject();
                    }
                    break;
                }
                wrtr.endObject();
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/exit")
    public JsonObject exit(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        Subscriber subscriber = getSubscriber(body.get("uuid").getAsString());
        subscriber.setLogin(null);
        subscriber.setLvlGr(null);
        try {
            wrtr.beginObject().name("error").value(false)
                .name("yes").value(false);
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/checkInvCode")
    public JsonObject checkInvCode(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = null;
        if(body.has("login")) {
            user = datas.userByLogin(body.get("login").getAsString());
        }
        Invite inv = datas.inviteByCode(body.get("code").getAsString());
        try {
            wrtr.beginObject().name("error").value(false);
            if (user != null) {
                user.getRoles().putAll(inv.getRole());
                datas.getUserRepository().saveAndFlush(user);
                School school = datas.schoolById(((Role) inv.getRole().values().toArray()[0]).getYO());
                if(ObjectUtils.isEmpty(school.getHteachersInv())) school.setHteachersInv(new ArrayList<>());
                if(ObjectUtils.isEmpty(school.getHteachers())) school.setHteachers(new ArrayList<>());
                school.getHteachersInv().remove(inv.getId());
                school.getHteachers().add(user.getId());
                datas.getSchoolRepository().saveAndFlush(school);
                datas.getInviteRepository().delete(inv);
                wrtr.name("yes").value(false);
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/checkReaCode")
    public JsonObject checkReaCode(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        User user = datas.userByCode(body.get("code").getAsString());
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null) wrtr.name("yes").value(false);
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        return datas.getObj(ans -> {}, wrtr, true);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody JsonObject body) throws Exception {
        System.out.println("Post! " + body);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        Subscriber subscriber = getSubscriber(body.get("uuid").getAsString());
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.get("id").getAsString());
        Invite inv = datas.inviteById(body.get("id1").getAsLong());
        Long schId = null;
        try {
            wrtr.beginObject().name("error").value(false);
            if(user != null && (user1 != null || inv != null)
                    && (body.get("role").getAsLong() == 3L && user.getRoles().containsKey(3L)
                    || body.get("role").getAsLong() == 4L && user.getRoles().containsKey(4L))) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if(user1 != null){
                    user1.setCode(uuid.toString());
                    user1.setExpDate(Main.df.format(dateAfter));
                    datas.getUserRepository().saveAndFlush(user1);
                    schId = datas.getFirstRole(user1.getRoles()).getYO();

                    wrtr.name("id").value(user1.getId());
                } else if(inv != null){
                    inv.setCode(uuid.toString());
                    inv.setExpDate(Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    schId = datas.getFirstRole(inv.getRole()).getYO();

                    wrtr.name("id").value(inv.getId());
                }
                System.out.println("setCode " + uuid);

                wrtr.name("code").value(uuid.toString())
                    .name("id1").value(schId);
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            wrtr.name("error").value(true);
        }
        Long finalSchId = schId;
        return datas.getObj(ans -> {
            sendMessageForAll("codPepL2C", ans, subscriber.getType(), "null", subscriber.getLvlGr(), "adm", "main");
            sendMessageForAll("codPepL1C", ans, subscriber.getType(), finalSchId +"", subscriber.getLvlGr(), "ht", "main");
        }, wrtr, true);
    }
}