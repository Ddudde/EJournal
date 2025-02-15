package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** RU: Контроллер для раздела новостей + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/NewsController">http://localhost:9001/swagger/htmlSwag/#/NewsController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/news")
@RequiredArgsConstructor
@RestController public class NewsController {
    private final DBService dbService;
    private final PushService pushService;
    private final SystRepository systRepository;
    private final NewsRepository newsRepository;
    private final SchoolRepository schoolRepository;
    private final MainService mainService;

    /** RU: удаление новости + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and ((#sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER'))
        or (#sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')))""")
    @DeleteMapping("/delNews")
    public ResponseEntity<Void> delNews(@RequestBody DataNews body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[DELETE] /delNews");
        final News news = dbService.newsById(body.id);
        final Syst syst = dbService.getSyst();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (news != null) {
            if (Objects.equals(sub.getLvlMore2(), "Por") && syst != null && !ObjectUtils.isEmpty(syst.getNews())) {
                syst.getNews().remove(news);
                systRepository.saveAndFlush(syst);
            }
            newsRepository.delete(news);
            wrtr.name("id").value(body.id);
            stat = HttpStatus.OK;
        }
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("delNewsC", ans, TypesConnect.NEWS, sub.getLvlSch(),
                sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: изменение новости + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and ((#sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER'))
        or (#sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')))""")
    @PutMapping("/chNews")
    public ResponseEntity<Void> chNews(@RequestBody DataNews body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PUT] /chNews");
        final News news = dbService.newsById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (news != null && !ObjectUtils.isEmpty(body.type)) {
            switch (body.type) {
                case "title" -> news.setTitle(body.val);
                case "date" -> news.setDate(body.val);
                case "img_url" -> news.setImg_url(body.val);
                case "text" -> news.setText(body.val);
                default -> {}
            }
            newsRepository.saveAndFlush(news);
            wrtr.name("id").value(body.id)
                .name("type").value(body.type)
                .name("val").value(body.val);
            stat = HttpStatus.OK;
        }
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chNewsC", ans, TypesConnect.NEWS, sub.getLvlSch(),
                sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        }, wrtr, stat);
    }

    private void writeNews(JsonTreeWriter wrtr, News news) throws IOException {
        wrtr.name("body").beginObject()
            .name("title").value(news.getTitle())
            .name("date").value(news.getDate())
            .name("img_url").value(news.getImg_url())
            .name("text").value(news.getText()).endObject()
            .name("id").value(news.getId());
    }

    /** RU: добавление новой новости учебного центра + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and #sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER')""")
    @PostMapping("/addNewsYo")
    public ResponseEntity<Void> addNewsYO(@RequestBody DataNews body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addNewsYo");
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (school != null && !ObjectUtils.isEmpty(body.date)) {
            final News news = newsRepository
                .saveAndFlush(new News(body.title, body.date, body.img_url, body.text));
            school.getNews().add(news);
            schoolRepository.saveAndFlush(school);
            writeNews(wrtr, news);
            stat = HttpStatus.CREATED;
        }
        return mainService.getObjR(ans -> {
            pushService.send(school.getId()+"News", "Новые объявления!",
                "В вашей школе новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
                "/DipvLom/static/media/info.jpg");
            SSEController.sendEventFor("addNewsC", ans, TypesConnect.NEWS, sub.getLvlSch(),
                sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: добавление новой новости портала + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and #sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')""")
    @PostMapping("/addNewsPor")
    public ResponseEntity<Void> addNewsPortal(@RequestBody DataNews body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addNewsPor");
        final Syst syst = dbService.getSyst();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (syst != null && !ObjectUtils.isEmpty(body.date)) {
            final News news = newsRepository
                .saveAndFlush(new News(body.title, body.date, body.img_url, body.text));
            syst.getNews().add(news);
            systRepository.saveAndFlush(syst);
            writeNews(wrtr, news);
            stat = HttpStatus.CREATED;
        }
        return mainService.getObjR(ans -> {
            pushService.send("news", "Новые объявления!",
                "На портале появилось новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
                "/DipvLom/static/media/info.jpg");
            SSEController.sendEventFor("addNewsC", ans, TypesConnect.NEWS, sub.getLvlSch(),
                sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: [start] отправка новостей, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @see DocsHelpController#point(Object, Object) Описание */
    @GetMapping("/getNews/{type}")
    public ResponseEntity<JsonObject> getNews(@PathVariable String type, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("type= "+type, "[GET] /getNews");
        List<News> list = null;
        final Syst syst = dbService.getSyst();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            Long schId = null;
        };
        if (user != null) {
            final School school = user.getSelecRole().getYO();
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
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.NEWS, ref.schId + "", "main", "main", type);
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые NewsController в методах
     * @see NewsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataNews {
        public final String type, title, date, img_url, text, val;
        public final Long id;
    }
}