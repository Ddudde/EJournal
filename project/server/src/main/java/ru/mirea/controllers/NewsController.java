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
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.util.List;
import java.util.Objects;

@RequestMapping("/news")
@NoArgsConstructor
@RestController public class NewsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/delNews")
    public JsonObject delNews(@RequestBody DataNews body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        News news = datas.newsById(body.id);
        Syst syst = datas.getSyst();
        try {
            body.wrtr = datas.ini(body.toString());
            if (user != null && news != null && syst != null
                    && (user.getRoles().containsKey(4L) && Objects.equals(subscriber.getLvlMore2(), "Por")
                    || user.getRoles().containsKey(3L) && Objects.equals(subscriber.getLvlMore2(), "Yo"))) {
                datas.getNewsRepository().delete(news);
                if (!ObjectUtils.isEmpty(syst.getNews())) syst.getNews().remove(news.getId());
                datas.getSystRepository().saveAndFlush(syst);
                body.wrtr.name("id").value(body.id);
            }
        } catch (Exception e) {
            body.bol = Main.excp(e);
        }
        return datas.getObj(ans -> {
            authController.sendMessageForAll("delNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/chNews")
    public JsonObject chNews(@RequestBody DataNews body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        try {
            body.wrtr = datas.ini(body.toString());
            User user = datas.userByLogin(subscriber.getLogin());
            News news = datas.newsById(body.id);
            if (user != null && news != null
                    && (user.getRoles().containsKey(4L) && Objects.equals(subscriber.getLvlMore2(), "Por")
                    || user.getRoles().containsKey(3L) && Objects.equals(subscriber.getLvlMore2(), "Yo"))) {
                switch (body.type) {
                    case "title" -> news.setTitle(body.val);
                    case "date" -> news.setDate(body.val);
                    case "img_url" -> news.setImg_url(body.val);
                    case "text" -> news.setText(body.val);
                    default -> {
                    }
                }
                datas.getNewsRepository().saveAndFlush(news);
                body.wrtr.name("id").value(body.id)
                        .name("type").value(body.type)
                        .name("val").value(body.val);
            }
        } catch (Exception e) {
            body.bol = Main.excp(e);
        }
        return datas.getObj(ans -> {
            authController.sendMessageForAll("chNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addNews")
    public JsonObject addNews(@RequestBody DataNews body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        try {
            body.wrtr = datas.ini(body.toString());
            if (subscriber != null) {
                body.wrtr.name("body").beginObject();
                User user = datas.userByLogin(subscriber.getLogin());
                Syst syst = datas.getSyst();
                School school = datas.schoolById(user.getRoles().get(user.getSelRole()).getYO());
                boolean b, b1;
                b = Objects.equals(subscriber.getLvlMore2(), "Por") && user.getRoles().containsKey(4L) && syst != null;
                b1 = Objects.equals(subscriber.getLvlMore2(), "Yo") && user.getRoles().containsKey(3L) && school != null;
                if (user != null && (b || b1)) {
                    News news = new News();
                    if (!ObjectUtils.isEmpty(body.title)) {
                        news.setTitle(body.title);
                    }
                    if (!ObjectUtils.isEmpty(body.date)) {
                        news.setDate(body.date);
                    }
                    if (!ObjectUtils.isEmpty(body.img_url)) {
                        news.setImg_url(body.img_url);
                    }
                    if (!ObjectUtils.isEmpty(body.text)) {
                        news.setText(body.text);
                    }
                    datas.getNewsRepository().saveAndFlush(news);
                    if (b) {
                        syst.getNews().add(news.getId());
                        datas.getSystRepository().saveAndFlush(syst);
                    } else if (b1) {
                        school.getNews().add(news.getId());
                        datas.getSchoolRepository().saveAndFlush(school);
                    }
                    body.wrtr.name("title").value(news.getTitle())
                        .name("date").value(news.getDate())
                        .name("img_url").value(news.getImg_url())
                        .name("text").value(news.getText()).endObject()
                        .name("id").value(news.getId());
                }
            }
        } catch (Exception e) {
            body.bol = Main.excp(e);
        }
        return datas.getObj(ans -> {
            if(!ObjectUtils.isEmpty(subscriber.getLvlSch())) {
                datas.getPushService().send(subscriber.getLvlSch()+"_news", "Новые объявления!", "В вашей школе новое объявление!\nУведомления можно регулировать на странице 'Настройки'", "/DipvLom/static/media/info.jpg");
            }
            authController.sendMessageForAll("addNewsC", ans, TypesConnect.NEWS, subscriber.getLvlSch(), subscriber.getLvlGr(), subscriber.getLvlMore1(), subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getNews")
    public JsonObject getNews(@RequestBody DataNews body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            List<Long> list = null;
            body.wrtr.name("body").beginObject();
            Syst syst = null;
            School school = null;
            if (subscriber != null) {
                User user = datas.userByLogin(subscriber.getLogin());
                syst = datas.getSyst();
                if (user != null) {
                    ref.schId = user.getRoles().get(user.getSelRole()).getYO();
                    school = datas.schoolById(ref.schId);
                    if (Objects.equals(body.type, "Yo") && school != null && !ObjectUtils.isEmpty(school.getNews())) {
                        list = school.getNews();
                    }
                }
                if (Objects.equals(body.type, "Por") && syst != null && !ObjectUtils.isEmpty(syst.getNews())) {
                    list = syst.getNews();
                    ref.schId = null;
                }
            }
            if (!ObjectUtils.isEmpty(list)) {
                for (Long i1 : list) {
                    News newsU = datas.newsById(i1);
                    if (newsU == null) {
                        if (ref.schId == null) {
                            syst.getNews().remove(i1);
                        } else {
                            school.getNews().remove(i1);
                        }
                        continue;
                    }
                    body.wrtr.name(i1 + "").beginObject()
                            .name("title").value(newsU.getTitle())
                            .name("date").value(newsU.getDate())
                            .name("img_url").value(newsU.getImg_url())
                            .name("text").value(newsU.getText())
                            .endObject();
                }
            }
            body.wrtr.endObject();
        } catch (Exception e) {
            body.bol = Main.excp(e);
        }
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, subscriber.getLogin(), TypesConnect.NEWS, ref.schId + "", "main", "main", body.type);
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataNews {
    public String uuid, type, title, date, img_url, text, val;
    public Long id;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}