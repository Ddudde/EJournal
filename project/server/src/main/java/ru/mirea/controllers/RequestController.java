package ru.mirea.controllers;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.school.Request;
import ru.mirea.data.models.auth.User;

@RestController
@RequestMapping("/requests")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000", "https://ddudde.github.io"})
public class RequestController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping
    public JsonObject post(@RequestBody JsonObject data) {
        System.out.println("Post! " + data);
        JsonObject ans = new JsonObject(), body = null, bodyAns;
        ans.addProperty("error", false);
        if(data.has("body") && data.get("body").isJsonObject()) body = data.get("body").getAsJsonObject();
        if(!data.has("type")) data.addProperty("type", "default");
        switch (data.get("type").getAsString()){
            case "getRequests" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null && user.getRoles().containsKey(4L)) {
                    for(Request reqR : datas.getRequests()){
                        JsonObject req = new JsonObject();
                        bodyAns.add(reqR.getId()+"", req);
                        req.addProperty("title", reqR.getEmail());
                        req.addProperty("date", reqR.getDate());
                        req.addProperty("text", reqR.getText());
                    }
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chText" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                Request request = datas.requestById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && request != null) {
                    request.setText(body.get("text").getAsString());
                    datas.getRequestRepository().saveAndFlush(request);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("id", request.getId());
                    ansToCl.addProperty("text", request.getText());
                    authController.sendMessageForAll("chText", ansToCl, TypesConnect.REQUESTS, "main", "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chDate" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                Request request = datas.requestById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && request != null) {
                    request.setDate(body.get("date").getAsString());
                    datas.getRequestRepository().saveAndFlush(request);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("id", request.getId());
                    ansToCl.addProperty("date", request.getDate());
                    authController.sendMessageForAll("chDate", ansToCl, TypesConnect.REQUESTS, "main", "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chTitle" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                Request request = datas.requestById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && request != null) {
                    request.setEmail(body.get("title").getAsString());
                    datas.getRequestRepository().saveAndFlush(request);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("id", request.getId());
                    ansToCl.addProperty("title", request.getEmail());
                    authController.sendMessageForAll("chTitle", ansToCl, TypesConnect.REQUESTS, "main", "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "delReq" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                Request request = datas.requestById(body.get("id").getAsLong());
                if(user != null && user.getRoles().containsKey(4L) && request != null) {
                    datas.getRequestRepository().delete(request);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("id", request.getId());
                    authController.sendMessageForAll("delReq", ansToCl, TypesConnect.REQUESTS, "main", "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "addReq" -> {
                bodyAns = new JsonObject();
                if(body.has("title") && body.has("dat") && body.has("text")) {
                    Request request = new Request(body.get("title").getAsString(), body.get("dat").getAsString(), body.get("text").getAsString());
                    datas.getRequestRepository().saveAndFlush(request);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("id", request.getId());
                    ansToCl.add("body", bodyAns);
                    bodyAns.addProperty("title", request.getEmail());
                    bodyAns.addProperty("date", request.getDate());
                    bodyAns.addProperty("text", request.getText());
                    authController.sendMessageForAll("addReq", ansToCl, TypesConnect.REQUESTS, "main", "main", "main", "main");
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