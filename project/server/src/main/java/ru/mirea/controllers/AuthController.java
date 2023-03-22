package ru.mirea.controllers;

import com.google.gson.JsonObject;
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
import ru.mirea.data.models.School;
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
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
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
//            fluxSink.error(new Exception("test"));
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

    @PostMapping
    public JsonObject post(@RequestBody JsonObject data) {
        System.out.println("Post! " + data);
        JsonObject ans = new JsonObject(), body = null, bodyAns;
        ans.addProperty("error", false);
        if(data.has("body") && data.get("body").isJsonObject()) body = data.get("body").getAsJsonObject();
        if(!data.has("type")) data.addProperty("type", "default");
        switch (data.get("type").getAsString()){
            case "infCon" -> {
                if(body.has("uuid")) {
                    if(body.has("login")) {
                        subscriptions.get(UUID.fromString(body.get("uuid").getAsString()))
                                .setLogin(body.get("login").getAsString());
                        System.out.println("setLog " + body.get("login").getAsString() + " subscription for " + body.get("uuid").getAsString());
                    }
                    if(body.has("type")) {
                        subscriptions.get(UUID.fromString(body.get("uuid").getAsString()))
                                .setType(TypesConnect.valueOf(body.get("type").getAsString()));
                        System.out.println("setType " + TypesConnect.valueOf(body.get("type").getAsString()) + " subscription for " + body.get("uuid").getAsString());
                    }
                    if(body.has("podType")) {
                        subscriptions.get(UUID.fromString(body.get("uuid").getAsString()))
                                .setLvlSch(body.get("podType").getAsString());
                        System.out.println("setPodType " + body.get("podType").getAsString() + " subscription for " + body.get("uuid").getAsString());
                    }
                }
                return ans;
            }
            case "remCon" -> {
                if(body.has("uuid")) {
                    subscriptions.remove(UUID.fromString(body.get("uuid").getAsString()));
                    System.out.println("subscription remCon " + body.get("uuid").getAsString() + " was closed");
                }
                return ans;
            }
            case "auth" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                bodyAns.addProperty("auth", false);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null) {
                    if(Objects.equals(user.getPassword(), body.get("password").getAsString())) {
                        bodyAns.addProperty("auth", true);
                        bodyAns.addProperty("login", user.getLogin());
//                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : ((Long) user.getRoles().keySet().toArray()[4]));
                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : datas.getFirstRoleId(user.getRoles()));
                        bodyAns.addProperty("ico", user.getIco());
                        bodyAns.addProperty("roles", !ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1);
                        bodyAns.addProperty("secFr", !ObjectUtils.isEmpty(user.getSecFr()));
                        infCon(body.get("uuid").getAsString(), body.get("login").getAsString(), null, null, null, null, null);
                    }
                }
                return ans;
            }
            case "reg" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                Invite inv = null;
                User user1 = null;
                if(Objects.equals(body.get("mod").getAsString(), "inv")){
                    inv = datas.inviteByCode(body.get("code").getAsString());
                }
                if(Objects.equals(body.get("mod").getAsString(), "rea")){
                    user1 = datas.userByCode(body.get("code").getAsString());
                }
                if(inv == null && user1 == null){
                    ans.addProperty("error", 2);
                    return ans;
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
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chPass" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null && Objects.equals(user.getSecFr(), body.get("secFr").getAsString())) {
                    user.setPassword(body.get("par").getAsString());
                    datas.getUserRepository().saveAndFlush(user);
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chRole" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                long curRol = body.get("role").getAsLong();
                if(user != null && user.getRoles().containsKey(curRol)) {
                    for(long i = (curRol == 4 ? 0 : curRol+1L); i < 5; i++){
                        if(!user.getRoles().containsKey(i)) continue;
                        bodyAns.addProperty("role", i);
                        Role role = user.getRoles().get(i);
                        if(role.getKids() != null && !role.getKids().isEmpty()){
                            bodyAns.addProperty("kid", role.getKids().get(0));
                            JsonObject kids = new JsonObject();
                            for(Long i1 : role.getKids()){
                                User kid = datas.userById(i1);
                                kids.addProperty(i1+"", kid.getFio());
                            }
                            bodyAns.add("kids", kids);
                        }
                        break;
                    }
                }
                return ans;
            }
            case "exit" -> {
                Subscriber subscriber = getSubscriber(body.get("uuid").getAsString());
                subscriber.setLogin(null);
                subscriber.setLvlGr(null);
                return ans;
            }
            case "checkInvCode" -> {
                User user = null;
                if(body.has("login")) {
                    user = datas.userByLogin(body.get("login").getAsString());
                }
                Invite inv = datas.inviteByCode(body.get("code").getAsString());
                if(inv == null) {
                    ans.addProperty("error", true);
                }else if (user != null) {
                    user.getRoles().putAll(inv.getRole());
                    datas.getUserRepository().saveAndFlush(user);
                    School school = datas.schoolById(((Role) inv.getRole().values().toArray()[0]).getYO());
                    if(ObjectUtils.isEmpty(school.getHteachersInv())) school.setHteachersInv(new ArrayList<>());
                    if(ObjectUtils.isEmpty(school.getHteachers())) school.setHteachers(new ArrayList<>());
                    school.getHteachersInv().remove(inv.getId());
                    school.getHteachers().add(user.getId());
                    datas.getSchoolRepository().saveAndFlush(school);
                    datas.getInviteRepository().delete(inv);
                }
                return ans;
            }
            case "checkReaCode" -> {
                User user = datas.userByCode(body.get("code").getAsString());
                if(user == null) {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "setCodePep" -> {
                Subscriber subscriber = getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                User user1 = datas.userByLogin(body.get("id").getAsString());
                Invite inv = datas.inviteById(body.get("id1").getAsLong());
                Long schId = null;
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

                        ans.addProperty("id", user1.getId());
                    } else if(inv != null){
                        inv.setCode(uuid.toString());
                        inv.setExpDate(Main.df.format(dateAfter));
                        datas.getInviteRepository().saveAndFlush(inv);
                        schId = datas.getFirstRole(inv.getRole()).getYO();

                        ans.addProperty("id", inv.getId());
                    }
                    System.out.println("setCode " + uuid);

                    ans.addProperty("code", uuid.toString());
                    ans.addProperty("id1", schId);
                    sendMessageForAll("codPepL2C", ans, subscriber.getType(), "null", subscriber.getLvlGr(), "adm", "main");
                    sendMessageForAll("codPepL1C", ans, subscriber.getType(), schId+"", subscriber.getLvlGr(), "ht", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            default -> {
                System.out.println("Error Type" + data.get("type"));
                ans.addProperty("error", true);
                return ans;
            }
        }
    }
}