package ru.mirea.controllers;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.ServerService;
import ru.mirea.data.models.News;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/news")
@NoArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000", "https://ddudde.github.io"})
public class NewsController {

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
            case "getNews" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                List<Long> list = null;
                bodyAns = new JsonObject();
                if(subscriber != null) {
                    ans.add("body", bodyAns);
                    User user = datas.userByLogin(subscriber.getLogin());
                    Syst syst = datas.getSyst();
                    Long schId = null;
                    if (user != null) {
                        schId = user.getRoles().get(body.get("role").getAsLong()).getYO();
                        School school = datas.schoolById(schId);
                        if (Objects.equals(body.get("type").getAsString(), "Yo") && school != null && !ObjectUtils.isEmpty(school.getNews())) {
                            list = school.getNews();
                        }
                    }
                    if (Objects.equals(body.get("type").getAsString(), "Por") && syst != null && !ObjectUtils.isEmpty(syst.getNews())) {
                        list = syst.getNews();
                        schId = null;
                    }
                    authController.infCon(body.get("uuid").getAsString(), subscriber.getLogin(), TypesConnect.NEWS, schId + "", "main", "main", body.get("type").getAsString());
                }
                if(!ObjectUtils.isEmpty(list)){
                    for (Long i1 : list) {
                        JsonObject newsO = new JsonObject();
                        News newsU = datas.newsById(i1);
                        newsO.addProperty("title", newsU.getTitle());
                        newsO.addProperty("date", newsU.getDate());
                        newsO.addProperty("img_url", newsU.getImg_url());
                        newsO.addProperty("text", newsU.getText());
                        bodyAns.add(i1 + "", newsO);
                    }
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "addNews" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                News news = null;
                if(subscriber != null) {
                    bodyAns = new JsonObject();
                    ans.add("body", bodyAns);
                    User user = datas.userByLogin(subscriber.getLogin());
                    Syst syst = datas.getSyst();
                    School school = datas.schoolById(user.getRoles().get(body.get("role").getAsLong()).getYO());
                    boolean b, b1;
                    b = Objects.equals(subscriber.getLvlSch(), "Por") && user.getRoles().containsKey(4L) && syst != null;
                    b1 = Objects.equals(subscriber.getLvlSch(), "Yo") && user.getRoles().containsKey(3L) && school != null;
                    if (user != null && (b || b1)) {
                        news = new News();
                        if (body.has("title")) news.setTitle(body.get("title").getAsString());
                        if (body.has("date")) news.setDate(body.get("date").getAsString());
                        if (body.has("img_url")) news.setImg_url(body.get("img_url").getAsString());
                        if (body.has("text")) news.setText(body.get("text").getAsString());
                        datas.getNewsRepository().saveAndFlush(news);
                        if (b) {
                            if (syst.getNews() == null) syst.setNews(new ArrayList<>());
                            syst.getNews().add(news.getId());
                            datas.getSystRepository().saveAndFlush(syst);
                        } else if (b1) {
                            if (school.getNews() == null) school.setNews(new ArrayList<>());
                            school.getNews().add(news.getId());
                            datas.getSchoolRepository().saveAndFlush(school);
                        }
                        ans.addProperty("id", news.getId());
                        bodyAns.addProperty("title", news.getTitle());
                        bodyAns.addProperty("date", news.getDate());
                        bodyAns.addProperty("img_url", news.getImg_url());
                        bodyAns.addProperty("text", news.getText());

                        authController.sendMessageForAll("addNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                    }
                }
                if(news == null) {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chNews" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                News news = datas.newsById(body.get("id").getAsLong());
                if(user != null && news != null
                        && (user.getRoles().containsKey(4L) && Objects.equals(subscriber.getLvlSch(), "Por")
                        || user.getRoles().containsKey(3L) && Objects.equals(subscriber.getLvlSch(), "Yo"))) {
                    switch (body.get("type").getAsString()){
                        case "title" -> news.setTitle(body.get("val").getAsString());
                        case "date" -> news.setDate(body.get("val").getAsString());
                        case "img_url" -> news.setImg_url(body.get("val").getAsString());
                        case "text" -> news.setText(body.get("val").getAsString());
                        default -> {}
                    }
                    datas.getNewsRepository().saveAndFlush(news);
                    ans.add("id", body.get("id"));
                    ans.add("type", body.get("type"));
                    ans.add("val", body.get("val"));

                    authController.sendMessageForAll("chNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "delNews" -> {
                Subscriber subscriber = authController.getSubscriber(body.get("uuid").getAsString());
                User user = datas.userByLogin(subscriber.getLogin());
                News news = datas.newsById(body.get("id").getAsLong());
                Syst syst = datas.getSyst();
                if(user != null && news != null && syst != null
                        && (user.getRoles().containsKey(4L) && Objects.equals(subscriber.getLvlSch(), "Por")
                        || user.getRoles().containsKey(3L) && Objects.equals(subscriber.getLvlSch(), "Yo"))) {
                    datas.getNewsRepository().delete(news);
                    if(!ObjectUtils.isEmpty(syst.getNews())) syst.getNews().remove(news.getId());
                    datas.getSystRepository().saveAndFlush(syst);
                    ans.add("id", body.get("id"));

                    authController.sendMessageForAll("delNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
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