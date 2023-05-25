package ru.mirea.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.ServerService;

import java.util.Objects;

@RequestMapping("/settings")
@NoArgsConstructor
@RestController public class SettingsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/chPass")
    public JsonObject chPass(@RequestBody DataSettings body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                if(!Objects.equals(user.getSecFr(), body.secFR)){
                    body.wrtr.name("error").value(3);
                } else if(!Objects.equals(user.getPassword(), body.oPar)){
                    body.wrtr.name("error").value(2);
                } else {
                    user.setPassword(body.nPar);
                    datas.getUserRepository().saveAndFlush(user);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chSecFR")
    public JsonObject chSecFR(@RequestBody DataSettings body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                user.setSecFr(body.secFR);
                datas.getUserRepository().saveAndFlush(user);
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chIco")
    public JsonObject chIco(@RequestBody DataSettings body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                user.setIco(body.ico);
                datas.getUserRepository().saveAndFlush(user);
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/setInfo")
    public JsonObject setInfo(@RequestBody DataSettings body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        try {
            body.wrtr = datas.ini(body.toString());
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.SETTINGS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataSettings {
    public String uuid, secFR, oPar, nPar;
    public int ico;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}