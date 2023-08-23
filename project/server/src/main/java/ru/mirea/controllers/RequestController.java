package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Request;
import ru.mirea.services.ServerService;

@RequestMapping("/requests")
@NoArgsConstructor
@RestController public class RequestController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addReq")
    public JsonObject addReq(@RequestBody DataRequest body) {
        try {
            body.wrtr = datas.ini(body.toString());
            if(!ObjectUtils.isEmpty(body.email) && !ObjectUtils.isEmpty(body.date) && !ObjectUtils.isEmpty(body.fio)) {
                Request request = new Request(body.email, body.date, body.fio);
                datas.getDbService().getRequestRepository().saveAndFlush(request);

                body.wrtr.name("id").value(request.getId())
                    .name("body").beginObject()
                    .name("title").value(request.getEmail())
                    .name("date").value(request.getDate())
                    .name("text").value(request.getFio())
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/delReq")
    public JsonObject delReq(@RequestBody DataRequest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Request request = datas.getDbService().requestById(body.id);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(4L) && request != null) {
                datas.getDbService().getRequestRepository().delete(request);

                body.wrtr.name("id").value(request.getId());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("delReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chTitle")
    public JsonObject chTitle(@RequestBody DataRequest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Request request = datas.getDbService().requestById(body.id);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(4L) && request != null) {
                request.setEmail(body.title);
                datas.getDbService().getRequestRepository().saveAndFlush(request);

                body.wrtr.name("id").value(request.getId())
                    .name("title").value(request.getEmail());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chTitle", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chDate")
    public JsonObject chDate(@RequestBody DataRequest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Request request = datas.getDbService().requestById(body.id);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(4L) && request != null) {
                request.setDate(body.date);
                datas.getDbService().getRequestRepository().saveAndFlush(request);

                body.wrtr.name("id").value(request.getId())
                    .name("date").value(request.getDate());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chDate", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chText")
    public JsonObject chText(@RequestBody DataRequest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        Request request = datas.getDbService().requestById(body.id);
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(4L) && request != null) {
                request.setFio(body.text);
                datas.getDbService().getRequestRepository().saveAndFlush(request);

                body.wrtr.name("id").value(request.getId())
                    .name("text").value(request.getFio());
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chText", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getRequests")
    public JsonObject getRequests(@RequestBody DataRequest body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(4L)) {
                body.wrtr.name("body").beginObject();
                for(Request reqR : datas.getDbService().getRequests()){
                    body.wrtr.name(reqR.getId()+"").beginObject()
                        .name("title").value(reqR.getEmail())
                        .name("date").value(reqR.getDate())
                        .name("text").value(reqR.getFio())
                        .endObject();
                }
                body.wrtr.endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataRequest {
    public String uuid, text, date, title, email, fio;
    public Long id;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}