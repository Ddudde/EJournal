package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.security.CustomToken;

import java.util.List;
import java.util.Objects;

import static ru.mirea.Main.datas;

/** RU: Контроллер для новостей + Server Sent Events
 * <pre>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Тестирование
 *  + Переписка2
 * </pre>
 * @see Subscriber */
@Api(tags = {"NewsController"})
@SwaggerDefinition(tags = {
    @Tag(name = "NewsController", description = "Контроллер для новостей + Server Sent Events")
})
@RequestMapping("/news")
@RequiredArgsConstructor
@RestController public class NewsController {

    /** RU: Контроллер авторизации + сервис */
    private final AuthController authController;

    /** RU: удаление новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @DeleteMapping(value = "/delNews")
    public ResponseEntity<JsonObject> delNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
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
            authController.sendMessageFor("delNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: изменение новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, val, type
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping(value = "/chNews")
    public ResponseEntity<JsonObject> chNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
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
            authController.sendMessageFor("chNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: добавление новой новости + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: title, date, img_url, text
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @ApiOperation(value = "отправка новостей, портала/школы", notes = "Более подробное описание...")
    @ApiResponses({
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 201, message = "Не авторизован"),
            @ApiResponse(code = 401, message = "Не авторизован"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 404, message = "Что-то не найдено"),
            @ApiResponse(code = 500, message = "Ошибка сервера, обычно JSON")
    })
    @PostMapping(value = "/addNews")
    public ResponseEntity<JsonObject> addNews(@RequestBody DataNews body, CustomToken auth) throws Exception {
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
            authController.sendMessageFor("addNewsC", ans, TypesConnect.NEWS, auth.getSub().getLvlSch(),
                auth.getSub().getLvlGr(), auth.getSub().getLvlMore1(), auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: отправка новостей, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @ApiOperation(value = "отправка новостей, портала/школы", notes = "Более подробное описание...", response = EmptyInterface.class)
    @ApiResponses({
        @ApiResponse(code = 401, message = "Не авторизован"),
        @ApiResponse(code = 403, message = "Нет прав"),
        @ApiResponse(code = 404, message = "Что-то не найдено"),
        @ApiResponse(code = 500, message = "Ошибка сервера, обычно JSON")
    })
    @GetMapping(value = "/getNews/{type}")
    public ResponseEntity<JsonObject> getNews(
        @ApiParam("Нужный тип: Por - портал, Yo - школы")
        @PathVariable String type, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init("type= "+type, "[GET] /getNews");
        List<News> list = null;
        Syst syst = datas.getDbService().getSyst();
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
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), ref.log, TypesConnect.NEWS, ref.schId + "", "main", "main", type);
        }, wrtr, HttpStatus.OK, false);
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