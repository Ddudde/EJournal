package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.db.IniDBService;

import static ru.mirea.Main.datas;

@RequestMapping("/test")
@RequiredArgsConstructor
@RestController public class TestController {

    private final AuthController authController;

    private final IniDBService iniDBService;

    @PostMapping(value = "/chBool")
    public JsonObject chBool(@RequestBody DataTest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(4L)) {
                switch (body.id) {
                    case "checkbox_debug" -> Main.debug = body.val;
                    case "checkbox_test" -> {
                        Main.test = body.val;
                        if(Main.test) {
                            iniDBService.testOn();
                        } else {
                            iniDBService.testOff();
                        }
                        iniDBService.getTestInfo(body.wrtr);
                    }
                    default -> {}
                }
                body.wrtr.name("yes").value(true);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataTest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(4L)) {
                body.wrtr.name("bodyS").beginObject()
                    .name("checkbox_debug").value(Main.debug)
                    .name("checkbox_test").value(Main.test)
                    .endObject();
                iniDBService.getTestInfo(body.wrtr);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.TEST, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataTest {
    public String uuid, id;
    public Boolean val;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}