package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.News;
import ru.data.models.Syst;
import ru.data.models.auth.User;
import ru.data.models.school.School;
import ru.security.CustomToken;

import java.util.List;
import java.util.Objects;

import static ru.Main.datas;

/** RU: Контроллер для раздела новостей + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/NewsController">http://localhost:9001/swagger/htmlSwag/#/NewsController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Тестирование
 *  + Переписка2
 *  - Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/news")
@RequiredArgsConstructor
@RestController public class NewsController {

    private final AuthController authController;

    /** RU: удаление новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @DeleteMapping("/delNews")
    public ResponseEntity<Void> delNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /delNews");
        User user = auth.getSub().getUser();
        News news = datas.getDbService().newsById(body.id);
        Syst syst = datas.getDbService().getSyst();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && news != null
            && (user.getRoles().containsKey(4L) && Objects.equals(auth.getSub().getLvlMore2(), "Por") && syst != null
            || user.getRoles().containsKey(3L) && Objects.equals(auth.getSub().getLvlMore2(), "Yo"))) {
            datas.getDbService().getNewsRepository().delete(news);
            if (syst != null && !ObjectUtils.isEmpty(syst.getNews())) {
                syst.getNews().remove(news);
                datas.getDbService().getSystRepository().saveAndFlush(syst);
            }
            wrtr.name("id").value(body.id);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("delNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: изменение новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, val, type
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chNews")
    public ResponseEntity<Void> chNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH]  /chNews");
        User user = auth.getSub().getUser();
        News news = datas.getDbService().newsById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && news != null
            && (user.getRoles().containsKey(4L) && Objects.equals(auth.getSub().getLvlMore2(), "Por")
            || user.getRoles().containsKey(3L) && Objects.equals(auth.getSub().getLvlMore2(), "Yo"))
            && !ObjectUtils.isEmpty(body.type)) {
            switch (body.type) {
                case "title" -> news.setTitle(body.val);
                case "date" -> news.setDate(body.val);
                case "img_url" -> news.setImg_url(body.val);
                case "text" -> news.setText(body.val);
                default -> {}
            }
            datas.getDbService().getNewsRepository().saveAndFlush(news);
            wrtr.name("id").value(body.id)
                .name("type").value(body.type)
                .name("val").value(body.val);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: добавление новой новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: title, date, img_url, text
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping("/addNews")
    public ResponseEntity<Void> addNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addNews");
        User user = auth.getSub().getUser();
        Syst syst = datas.getDbService().getSyst();
        School school = user.getSelecRole().getYO();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            final boolean b = Objects.equals(auth.getSub().getLvlMore2(), "Por")
                    && user.getRoles().containsKey(4L) && syst != null,
                b1 = Objects.equals(auth.getSub().getLvlMore2(), "Yo")
                    && user.getRoles().containsKey(3L) && school != null;
        };
        if (user != null && (ref.b || ref.b1) && !ObjectUtils.isEmpty(body.date)) {
            News news = datas.getDbService().getNewsRepository()
                .saveAndFlush(new News(body.title, body.date, body.img_url,
                    body.text));
            if (ref.b) {
                syst.getNews().add(news);
                datas.getDbService().getSystRepository().saveAndFlush(syst);
            } else if (ref.b1) {
                school.getNews().add(news);
                datas.getDbService().getSchoolRepository().saveAndFlush(school);
            }
            wrtr.name("body").beginObject()
                .name("title").value(news.getTitle())
                .name("date").value(news.getDate())
                .name("img_url").value(news.getImg_url())
                .name("text").value(news.getText()).endObject()
                .name("id").value(news.getId());
            stat = HttpStatus.CREATED;
        }
        return datas.getObjR(ans -> {
            if(ref.b1 && !ObjectUtils.isEmpty(auth.getSub().getLvlSch())) {
                datas.getPushService().send(auth.getSub().getLvlSch()+"News", "Новые объявления!",
                "В вашей школе новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
                "/DipvLom/static/media/info.jpg");
            } else if(ref.b) {
                datas.getPushService().send("news", "Новые объявления!",
                "На портале появилось новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
                "/DipvLom/static/media/info.jpg");
            }
            authController.sendEventFor("addNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: [start] отправка новостей, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getNews/{type}")
    public ResponseEntity<JsonObject> getNews(@PathVariable String type, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init("type= "+type, "[GET] /getNews");
        List<News> list = null;
        Syst syst = datas.getDbService().getSyst();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            Long schId = null;
            String log = null;
        };
        if (user != null) {
            ref.log = user.getUsername();
            School school = user.getSelecRole().getYO();
            if (Objects.equals(type, "Yo") && school != null) {
                ref.schId = school.getId();
                list = school.getNews();
            }
        }
        if (Objects.equals(type, "Por") && syst != null) {
            list = syst.getNews();
        }
        if (!ObjectUtils.isEmpty(list)) {
            for (News newsU : list) {
                wrtr.name(newsU.getId() + "").beginObject()
                    .name("title").value(newsU.getTitle())
                    .name("date").value(newsU.getDate())
                    .name("img_url").value(newsU.getImg_url())
                    .name("text").value(newsU.getText())
                    .endObject();
            }
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), ref.log, TypesConnect.NEWS, ref.schId + "", "main", "main", type);
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые NewsController в методах
     * @see NewsController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataNews {
        public String type, title, date, img_url, text, val;
        public Long id;
    }
}