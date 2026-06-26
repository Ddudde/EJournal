package ru.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.news.NewsInnerDTO;
import ru.data.DTO.controller.news.NewsOutBodyDTO;
import ru.data.DTO.controller.news.NewsOutDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.INewsService;
import ru.services.interfaces.logic.ISSEService;

import java.util.List;
import java.util.Map;

/** RU: Контроллер для раздела новостей + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/NewsController">http://localhost:9001/swagger/htmlSwag/#/NewsController</a>
 * </pre>
 * @see SubscriberDTO */
@RequiredArgsConstructor
@RestController
@RequestMapping("/news")
public class NewsController {
    private final IDBService dbService;
    private final INewsService newsService;
    private final ISSEService sseService;

    /** RU: удаление новости + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and ((#sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER'))
        or (#sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')))""")
    @DeleteMapping("/delNews")
    public ResponseEntity<Void> delNews(@RequestBody NewsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final News news = dbService.newsById(body.id);
        final Syst syst = dbService.getSyst();
        if (news == null) return ResponseEntity.notFound().build();

        final NewsOutDTO outDTO = newsService.deleteNews(body, sub.getLvlMore2(), syst, news);
        sseService.sendEventFor(auth.getUserId(), "delNewsC", outDTO, TypesConnect.NEWS, sub.getLvlSch(),
            sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        return ResponseEntity.ok().build();
    }

    /** RU: изменение новости + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and ((#sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER'))
        or (#sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')))""")
    @PutMapping("/chNews")
    public ResponseEntity<Void> chNews(@RequestBody NewsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final News news = dbService.newsById(body.id);
        if (news == null || ObjectUtils.isEmpty(body.type)) return ResponseEntity.notFound().build();

        final NewsOutDTO outDTO = newsService.changeNews(body, news);
        sseService.sendEventFor(auth.getUserId(), "chNewsC", outDTO, TypesConnect.NEWS, sub.getLvlSch(),
            sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        return ResponseEntity.ok().build();
    }

    /** RU: добавление новой новости учебного центра + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and #sub.getLvlMore2() == 'Yo' and hasAuthority('HTEACHER')""")
    @PostMapping("/addNewsYo")
    public ResponseEntity<Void> addNewsYO(@RequestBody NewsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        if (school == null || ObjectUtils.isEmpty(body.date)) return ResponseEntity.notFound().build();

        final NewsOutDTO outDTO = newsService.addNewsYO(body, school);
        sseService.sendEventFor(auth.getUserId(), "addNewsC", outDTO, TypesConnect.NEWS, sub.getLvlSch(),
            sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: добавление новой новости портала + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and #sub.getLvlMore2() == 'Por' and hasAuthority('ADMIN')""")
    @PostMapping("/addNewsPor")
    public ResponseEntity<Void> addNewsPortal(@RequestBody NewsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final Syst syst = dbService.getSyst();
        if (syst == null || ObjectUtils.isEmpty(body.date)) return ResponseEntity.notFound().build();

        final NewsOutDTO outDTO = newsService.addNewsPortal(body, syst);
        sseService.sendEventFor(auth.getUserId(), "addNewsC", outDTO, TypesConnect.NEWS, sub.getLvlSch(),
            sub.getLvlGr(), sub.getLvlMore1(), sub.getLvlMore2());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: [start] отправка новостей, портала/школы
     * @see DocsHelpController#point Описание */
    @GetMapping("/getNews/Yo")
    public ResponseEntity<Map<Long, NewsOutBodyDTO>> getNewsYo(AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        List<News> list = null;
        Long schId = null;
        if (user != null) {
            final School school = user.getSelecRole().getYO();
            if (school != null) {
                schId = school.getId();
                list = school.getNews();
            }
        }
        if (ObjectUtils.isEmpty(list)) return ResponseEntity.notFound().build();

        final Map<Long, NewsOutBodyDTO> outDTO = newsService.prepareNews(list);
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.NEWS, schId + "", "main", "main", "Yo");
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] отправка новостей, портала/школы
     * @see DocsHelpController#point Описание */
    @GetMapping("/getNews/Por")
    public ResponseEntity<Map<Long, NewsOutBodyDTO>> getNewsPor(AuthToken auth) {
        List<News> list = null;
        final Syst syst = dbService.getSyst();
        if (syst != null) {
            list = syst.getNews();
        }
        if (ObjectUtils.isEmpty(list)) return ResponseEntity.notFound().build();

        final Map<Long, NewsOutBodyDTO> outDTO = newsService.prepareNews(list);
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.NEWS, "null", "main", "main", "Por");
        return ResponseEntity.ok(outDTO);
    }

}