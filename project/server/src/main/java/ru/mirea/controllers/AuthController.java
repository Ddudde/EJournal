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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.security.AuthenticationProvider;
import ru.mirea.services.security.JwtProvider;
import ru.mirea.services.ServerService;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/auth")
@NoArgsConstructor
@RestController public class AuthController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    private final Map<UUID, Subscriber> subscriptions = new ConcurrentHashMap<>();

    @GetMapping(path = "/open-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> openSseStream() {
        System.out.println("YT3 " + SecurityContextHolder.getContext().getAuthentication());
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
        final var ref = new Object() {
            UUID id;
        };
        try {
            subscriptions.forEach((uuid, subscriber) -> {
                if ((type == TypesConnect.MAIN || Objects.equals(type, subscriber.getType()))
                 && (Objects.equals(lvlSch, "main") || Objects.equals(lvlSch, subscriber.getLvlSch()))
                 && (Objects.equals(lvlGr, "main") || Objects.equals(lvlGr, subscriber.getLvlGr()))
                 && (Objects.equals(lvlMore1, "main") || Objects.equals(lvlMore1, subscriber.getLvlMore1()))
                 && (Objects.equals(lvlMore2, "main") || Objects.equals(lvlMore2, subscriber.getLvlMore2()))) {
                    ref.id = uuid;
                    subscriber.getFluxSink().next(event);
                }
            });
        } catch (Error e) {
            subscriptions.remove(ref.id);
            System.out.println("subscription " + ref.id + " was closed from Ping");
        }
    }

    public Subscriber getSubscriber(String uuid) {
        if(!ObjectUtils.isEmpty(uuid)) {
            return subscriptions.get(UUID.fromString(uuid));
        }
        return null;
    }

    public void infCon(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid != null) {
            Subscriber sub = subscriptions.get(UUID.fromString(uuid));
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
            User user = null;
            if(subscriber != null) {
                user = datas.getDbService().userByLogin(subscriber.getLogin());
            }
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
                body.wrtr.name("body").beginObject()
                    .name("role").value(user.getSelRole());
                if (user.getSelRole() == 1L) {
                    Role role = user.getRoles().get(1L);
                    body.wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (User kid : role.getKids()) {
                            body.wrtr.name(kid.getId() + "").value(kid.getFio());
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

    @PostMapping(value = "/checkCodeEmail")
    public JsonObject checkCodeEmail(@RequestBody DataAuth body) {
        try {
            body.wrtr = datas.ini(body.toString());
            User user = null;
            if(!ObjectUtils.isEmpty(body.invCod)) {
                user = datas.getDbService().userByCode(body.invCod);
            } else if(user == null && !ObjectUtils.isEmpty(body.uuid)) {
                Subscriber subscriber = getSubscriber(body.uuid);
                user = datas.getDbService().userByLogin(subscriber.getLogin());
            }
            if(user == null && !ObjectUtils.isEmpty(body.uuid)) {
                Subscriber subscriber = getSubscriber(body.uuid);
                user = datas.getDbService().userByLogin(subscriber.getLogin());
            }
            if(user != null && Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
                user.getSettings().setEmail(body.email);
                user.getSettings().setEmailCode(null);
                user.getSettings().setExpDateEC(null);
                datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/startEmail")
    public JsonObject startEmail(@RequestBody DataAuth body) {
        try {
            body.wrtr = datas.ini(body.toString());
            User user = null;
            if(!ObjectUtils.isEmpty(body.invCod)) {
                user = datas.getDbService().userByCode(body.invCod);
            } else if(user == null && !ObjectUtils.isEmpty(body.uuid)) {
                Subscriber subscriber = getSubscriber(body.uuid);
                user = datas.getDbService().userByLogin(subscriber.getLogin());
            }
            if(user != null) {
                String code = UUID.randomUUID().toString();
                datas.getEmailService().sendRegCode(body.email, code);
                user.getSettings().setEmailCode(code);
                Instant after = Instant.now().plus(Duration.ofDays(1));
                Date dateAfter = Date.from(after);
                user.getSettings().setExpDateEC(Main.df.format(dateAfter));
                datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/auth")
    public JsonObject auth(@RequestBody DataAuth body) {
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("body").beginObject();
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

                UUID uuid = UUID.randomUUID();
                Subscriber subscriber = new Subscriber(user.getUsername());
                authenticationProvider.subscriptions.put(uuid, subscriber);

                body.wrtr.name("auth").value(true)
                    .name("login").value(user.getUsername())
//                        bodyAns.addProperty("role", ObjectUtils.isEmpty(user.getRoles()) ? 0 : ((Long) user.getRoles().keySet().toArray()[4]));
                    .name("role").value(user.getSelRole())
                    .name("uuidS").value(String.valueOf(uuid))
                    .name("ico").value(user.getSettings().getIco())
                    .name("roles").value(!ObjectUtils.isEmpty(user.getRoles()) && user.getRoles().size() > 1)
                    .name("secFr").value(!ObjectUtils.isEmpty(user.getSettings().getSecFr()))
                    .name("email").value(!ObjectUtils.isEmpty(user.getSettings().getEmail()));
                if(user.getSelRole() == 1L) {
                    Role role = user.getRoles().get(1L);
                    body.wrtr.name("kid").value(user.getSelKid())
                        .name("kids").beginObject();
                    if (!ObjectUtils.isEmpty(role.getKids())) {
                        for (User kid : role.getKids()) {
                            body.wrtr.name(kid.getId() + "").value(kid.getFio());
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
        User user = datas.getDbService().userByLogin(body.login),
            user1 = datas.getDbService().userByCode(body.code);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user1 == null){
                body.wrtr.name("error").value(2);
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
            }
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/checkPasCodeEmail")
    public JsonObject checkPasCodeEmail(@RequestBody DataAuth body) {
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            SettingUser settingUser = user.getSettings();
            if(user != null && Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
                user.getSettings().setEmailCode(null);
                user.getSettings().setExpDateEC(null);
                datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
                user.setPassword(body.par);
                datas.getDbService().getUserRepository().saveAndFlush(user);
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chPass")
    public JsonObject chPass(@RequestBody DataAuth body) {
        User user = datas.getDbService().userByLogin(body.login);
        try {
            body.wrtr = datas.ini(body.toString());
            SettingUser settingUser = user.getSettings();
            if(user != null
            && ((!body.emailSt && Objects.equals(settingUser.getSecFr(), body.secFr))
            || (body.emailSt && Objects.equals(user.getSettings().getEmail(), body.email)))) {
                if(body.emailSt) {
                    String code = UUID.randomUUID().toString();
                    datas.getEmailService().sendRecCode(body.email, code, "Восстановление пароля в EJournal");
                    user.getSettings().setEmailCode(code);
                    Instant after = Instant.now().plus(Duration.ofDays(1));
                    Date dateAfter = Date.from(after);
                    user.getSettings().setExpDateEC(Main.df.format(dateAfter));
                    datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
                } else {
                    user.setPassword(body.par);
                    datas.getDbService().getUserRepository().saveAndFlush(user);
                }
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chKid")
    public JsonObject chKid(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getSelRole() == 1L && body.idL != null) {
                user.setSelKid(body.idL);
                body.wrtr.name("kid").value(user.getSelKid());
                datas.getDbService().getUserRepository().saveAndFlush(user);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chRole")
    public JsonObject chRole(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        long curRol = user.getSelRole();
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(curRol)) {
                body.wrtr.name("body").beginObject();
                for(long i = (curRol == 4 ? 0 : curRol+1L); i < 5; i++){
                    if(!user.getRoles().containsKey(i)) continue;
                    body.wrtr.name("role").value(i);
                    user.setSelRole(i);
                    datas.getDbService().getUserRepository().saveAndFlush(user);
                    Role role = user.getRoles().get(i);
                    if(i == 1L) {
                        body.wrtr.name("kid").value(user.getSelKid())
                            .name("kids").beginObject();
                        if (!ObjectUtils.isEmpty(role.getKids())) {
                            for (User kid : role.getKids()) {
                                body.wrtr.name(kid.getId() + "").value(kid.getFio());
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
        if(subscriber != null) {
            if (!ObjectUtils.isEmpty(body.notifToken)) {
                User user = datas.getDbService().userByLogin(subscriber.getLogin());
                if(user != null) {
                    SettingUser settingUser = user.getSettings();
                    datas.getPushService().remToken(settingUser, body.notifToken);
                    datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                }
            }
            subscriber.setLogin(null);
            subscriber.setLvlGr(null);
        }
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/remNotifToken")
    public JsonObject remNotifToken(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            User user = datas.getDbService().userByLogin(subscriber.getLogin());
            SettingUser settingUser = user.getSettings();
            datas.getPushService().remToken(settingUser, body.notifToken);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
        }
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addNotifToken")
    public JsonObject addNotifToken(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            User user = datas.getDbService().userByLogin(subscriber.getLogin());
            SettingUser settingUser = user.getSettings();
            datas.getPushService().addToken(settingUser, body.notifToken);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
        }
        try {
            body.wrtr = datas.ini(body.toString());
            body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/checkInvCode")
    public JsonObject checkInvCode(@RequestBody DataAuth body) {
        User inv = datas.getDbService().userByCode(body.code);
        try {
            body.wrtr = datas.ini(body.toString());
            if(inv != null) body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/checkReaCode")
    public JsonObject checkReaCode(@RequestBody DataAuth body) {
        User user = datas.getDbService().userByCode(body.code);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) body.wrtr.name("yes").value(true);
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setCodePep")
    public JsonObject setCodePep(@RequestBody DataAuth body) {
        Subscriber subscriber = getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        User user1 = datas.getDbService().userByLogin(body.id);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
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

                body.wrtr.name("id").value(user1.getId());
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
        password, type, id, email, invCod, emailCode;
    public Long id1, idL;
    public int ico;
    public boolean permis, emailSt;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}