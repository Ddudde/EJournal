package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.json.Role;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/auth")
@NoArgsConstructor
@RestController public class AuthController {

    @Autowired
    private ServerService datas;

    private final Map<UUID, Subscriber> subscriptions = new ConcurrentHashMap<>();

    @GetMapping(path = "/open-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> openSseStream() {
        Flux<ServerSentEvent> stream = Flux.create(fluxSink -> {
            UUID uuid = UUID.randomUUID();
            Subscriber subscriber = new Subscriber(fluxSink);
            subscriptions.put(uuid, subscriber);
            System.out.println("create subscription for " + uuid);
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                .event("chck").data(uuid).build();
            fluxSink.next(event);
            fluxSink.onCancel(() -> {
                    subscriptions.remove(uuid);
                    System.out.println("subscription " + uuid + " was closed from onCancel");
            });
            fluxSink.onDispose(() -> {
                    subscriptions.remove(uuid);
                    System.out.println("subscription " + uuid + " was closed from onDispose");
            });
            ServerSentEvent<Object> event1 = ServerSentEvent.builder()
                .event("connect").data("").build();
            fluxSink.next(event1);
        });
        return stream;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void ping(){
        sendMessageForAll("ping", "test", TypesConnect.MAIN, "main", "main", "main", "main");
    }

    public void sendMessageForAll(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        ServerSentEvent<Object> event = ServerSentEvent.builder()
            .event(evName).data(data).build();
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
                subscriptions.get(UUID.fromString(uuid)).setLogin(login);
                System.out.println("setLog " + login + " subscription for " + uuid);
            }
            if(type != null) {
                subscriptions.get(UUID.fromString(uuid)).setType(type);
                System.out.println("setType " + type + " subscription for " + uuid);
            }
            if(lvlSch != null) {
                subscriptions.get(UUID.fromString(uuid)).setLvlSch(lvlSch);
                System.out.println("setLvlSch " + lvlSch + " subscription for " + uuid);
            }
            if(lvlGr != null) {
                subscriptions.get(UUID.fromString(uuid)).setLvlGr(lvlGr);
                System.out.println("setLvlGr " + lvlGr + " subscription for " + uuid);
            }
            if(lvlMore1 != null) {
                subscriptions.get(UUID.fromString(uuid)).setLvlMore1(lvlMore1);
                System.out.println("setLvlMore1 " + lvlMore1 + " subscription for " + uuid);
            }
            if(lvlMore2 != null) {
                subscriptions.get(UUID.fromString(uuid)).setLvlMore2(lvlMore2);
                System.out.println("setLvlMore2 " + lvlMore2 + " subscription for " + uuid);
            }
        }
    }

    @PostMapping(value = "/infCon")
    public JsonObject infCon(@RequestBody DataAuth body) {
        try {
            body.wrtr = datas.ini(body.toString());
            TypesConnect type = null;
            if(!ObjectUtils.isEmpty(body.type)) {
                type = TypesConnect.valueOf(body.type);
            }
            infCon(body.uuid, body.login, type, null, null, null, null);
            Subscriber subscriber = getSubscriber(body.uuid);
            User user = datas.userByLogin(subscriber.getLogin());
            if(user != null) {
                if (!ObjectUtils.isEmpty(body.notifToken)) {
                    if(body.permis) {
                        datas.addToken(user, body.notifToken);
                    } else {
                        datas.remToken(user, body.notifToken);
                    }
                    datas.getUserRepository().saveAndFlush(user);
                }
                body.wrtr.name("body").beginObject()
                    .name("role").value(user.getSelRole());
                if (user.getSelRole() == 1L) {
                    Role role = user.getRoles().get(1L);
                    body.wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (Long i1 : role.getKids()) {
                            User kid = datas.userById(i1);
                            body.wrtr.name(i1 + "").value(kid.getFio());
                        }
                    }
                    if (!ObjectUtils.isEmpty(role.getKidsInv())) {
                        for (Long i1 : role.getKidsInv()) {
                            Invite kid = datas.inviteById(i1);
                            body.wrtr.name(i1 + "").value(kid.getFio());
                        }
                    }
                    body.wrtr.endObject();
                }
                body.wrtr.endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/remCon")
    public JsonObject remCon(@RequestBody DataAuth body) {
        try {
            body.wrtr = datas.ini(body.toString());
            if(!ObjectUtils.isEmpty(body.uuid)) {
                subscriptions.remove(UUID.fromString(body.uuid));
                System.out.println("subscription remCon " + body.uuid + " was closed");
            }
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/auth")
    public JsonObject auth(@RequestBody DataAuth body) {
        User user = datas.userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
            if(user != null && Objects.equals(user.getPassword(), body.password)) {
                if(!ObjectUtils.isEmpty(body.notifToken)) {
                    if(body.permis) {
                        datas.addToken(user, body.notifToken);
                    } else {
                        datas.remToken(user, body.notifToken);
                    }
                    datas.getUserRepository().saveAndFlush(user);
                }
                body.wrtr.name("auth").value(true)
                    .name("login").value(user.getLogin())
//                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : ((Long) user.getRoles().keySet().toArray()[4]));
                    .name("role").value(user.getSelRole())
                    .name("ico").value(user.getIco())
                    .name("roles").value(!ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1)
                    .name("secFr").value(!ObjectUtils.isEmpty(user.getSecFr()));
                if(user.getSelRole() == 1L) {
                    Role role = user.getRoles().get(1L);
                    body.wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (Long i1 : role.getKids()) {
                            User kid = datas.userById(i1);
                            body.wrtr.name(i1 + "").value(kid.getFio());
                        }
                    }
                    if (!ObjectUtils.isEmpty(role.getKidsInv())) {
                        for (Long i1 : role.getKidsInv()) {
                            Invite kid = datas.inviteById(i1);
                            body.wrtr.name(i1 + "").value(kid.getFio());
                        }
                    }
                    body.wrtr.endObject();
                }
                infCon(body.uuid, body.login, null, null, null, null, null);
            }
            body.wrtr.endObject();
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/reg")
    public JsonObject reg(@RequestBody DataAuth body) {
        User user = datas.userByLogin(body.login), user1 = null;
        Invite inv = null;
        if(Objects.equals(body.mod, "inv")){
            inv = datas.inviteByCode(body.code);
        }
        if(Objects.equals(body.mod, "rea")){
            user1 = datas.userByCode(body.code);
        }
        try {
            body.wrtr = datas.ini(body.toString());
            if(inv == null && user1 == null){
                body.wrtr.name("error").value(2);
                body.wrtr.name("yes").value(true);
                return datas.getObj(ans -> {}, body.wrtr, body.bol);
            }
            if(user == null) {
                if(inv != null) {
                    user = new User(body.login, body.par, body.ico);
                    user.setRoles(inv.getRole());
                    user.setSelRole(datas.getFirstRoleId(inv.getRole()));
                    if(inv.getRole().containsKey(1L)) {
                        if(!ObjectUtils.isEmpty(inv.getRole().get(1L).getKids())) {
                            user.setSelKid(inv.getRole().get(1L).getKids().get(0));
                        } else if(!ObjectUtils.isEmpty(inv.getRole().get(1L).getKidsInv())) {
                            user.setSelKid(inv.getRole().get(1L).getKidsInv().get(0));
                        }
                    }
                    user.setFio(inv.getFio());
                    Long schId = datas.getFirstRole(inv.getRole()).getYO();
                    datas.addTopic(user, schId+"_news");
                    datas.getUserRepository().saveAndFlush(user);
                    School school = datas.schoolById(schId);
                    school.getHteachersInv().remove(inv.getId());
                    school.getHteachers().add(user.getId());
                    datas.getSchoolRepository().saveAndFlush(school);
                    datas.getInviteRepository().delete(inv);
                }
                if(user1 != null){
                    user1.setLogin(body.login);
                    user1.setPassword(body.par);
                    user1.setIco(body.ico);
                    user1.setCode(null);
                    user1.setExpDate(null);
                    datas.getUserRepository().saveAndFlush(user1);
                }
            }
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPass")
    public JsonObject chPass(@RequestBody DataAuth body) {
        User user = datas.userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && Objects.equals(user.getSecFr(), body.secFr)) {
                user.setPassword(body.par);
                datas.getUserRepository().saveAndFlush(user);
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chKid")
    public JsonObject chKid(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getSelRole() == 1L && body.idL != null) {
                user.setSelKid(body.idL);
                body.wrtr.name("kid").value(user.getSelKid());
                datas.getUserRepository().saveAndFlush(user);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chRole")
    public JsonObject chRole(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        long curRol = user.getSelRole();
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(curRol)) {
                body.wrtr.name("body").beginObject();
                for(long i = (curRol == 4 ? 0 : curRol+1L); i < 5; i++){
                    if(!user.getRoles().containsKey(i)) continue;
                    body.wrtr.name("role").value(i);
                    user.setSelRole(i);
                    datas.getUserRepository().saveAndFlush(user);
                    Role role = user.getRoles().get(i);
                    if(i == 1L) {
                        body.wrtr.name("kid").value(user.getSelKid())
                            .name("kids").beginObject();
                        if (!ObjectUtils.isEmpty(role.getKids())) {
                            for (Long i1 : role.getKids()) {
                                User kid = datas.userById(i1);
                                body.wrtr.name(i1 + "").value(kid.getFio());
                            }
                        }
                        if (!ObjectUtils.isEmpty(role.getKidsInv())) {
                            for (Long i1 : role.getKidsInv()) {
                                Invite kid = datas.inviteById(i1);
                                body.wrtr.name(i1 + "").value(kid.getFio());
                            }
                        }
                        body.wrtr.endObject();
                    }
                    break;
                }
                body.wrtr.endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/exit")
    public JsonObject exit(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            User user = datas.userByLogin(subscriber.getLogin());
            datas.remToken(user, body.notifToken);
            datas.getUserRepository().saveAndFlush(user);
        }
        subscriber.setLogin(null);
        subscriber.setLvlGr(null);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/checkInvCode")
    public JsonObject checkInvCode(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        Invite inv = datas.inviteByCode(body.code);
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null) {
                user.getRoles().putAll(inv.getRole());
                datas.getUserRepository().saveAndFlush(user);
                School school = datas.schoolById(((Role) inv.getRole().values().toArray()[0]).getYO());
                school.getHteachersInv().remove(inv.getId());
                school.getHteachers().add(user.getId());
                datas.getSchoolRepository().saveAndFlush(school);
                datas.getInviteRepository().delete(inv);
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/checkReaCode")
    public JsonObject checkReaCode(@RequestBody DataAuth body) {
        User user = datas.userByCode(body.code);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        User user1 = datas.userByLogin(body.id);
        Invite inv = datas.inviteById(body.id1);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && (user1 != null || inv != null)
                    && (user.getSelRole() == 3L && user.getRoles().containsKey(3L)
                    || user.getSelRole() == 4L && user.getRoles().containsKey(4L))) {
                UUID uuid = UUID.randomUUID();
                Instant after = Instant.now().plus(Duration.ofDays(30));
                Date dateAfter = Date.from(after);
                if(user1 != null){
                    user1.setCode(uuid.toString());
                    user1.setExpDate(Main.df.format(dateAfter));
                    datas.getUserRepository().saveAndFlush(user1);
                    ref.schId = datas.getFirstRole(user1.getRoles()).getYO();

                    body.wrtr.name("id").value(user1.getId());
                } else if(inv != null){
                    inv.setCode(uuid.toString());
                    inv.setExpDate(Main.df.format(dateAfter));
                    datas.getInviteRepository().saveAndFlush(inv);
                    ref.schId = datas.getFirstRole(inv.getRole()).getYO();

                    body.wrtr.name("id").value(inv.getId());
                }
                System.out.println("setCode " + uuid);

                body.wrtr.name("code").value(uuid.toString())
                    .name("id1").value(ref.schId);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            sendMessageForAll("codPepL2C", ans, subscriber.getType(), "null", subscriber.getLvlGr(), "adm", "main");
            sendMessageForAll("codPepL1C", ans, subscriber.getType(), ref.schId +"", subscriber.getLvlGr(), "ht", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataAuth {
    public String uuid, code, notifToken, login, secFr, par, mod,
        password, type, id;
    public Long id1, idL;
    public int ico;
    public boolean permis;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}