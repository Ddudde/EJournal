package ru.controllers.school;

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
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.controllers.TypesConnect;
import ru.data.DAO.school.Request;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.school.RequestRepository;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;

/** RU: Контроллер для раздела заявок на подключение к системе учебных учреждений + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/RequestController">http://localhost:9001/swagger/htmlSwag/#/RequestController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/requests")
@RequiredArgsConstructor
@RestController public class RequestController {
    private final RequestRepository requestRepository;
    private final MainService mainService;
    private final DBService dbService;

    /** RU: добавляет заявку + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/addReq")
    public ResponseEntity<Void> addReq(@RequestBody DataRequest body) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addReq");
        if(ObjectUtils.isEmpty(body.email) || ObjectUtils.isEmpty(body.date) || ObjectUtils.isEmpty(body.fio)) {
            return ResponseEntity.notFound().build();
        }
        final Request request = new Request(body.email, body.date, body.fio);
        requestRepository.saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("body").beginObject()
            .name("title").value(request.getEmail())
            .name("date").value(request.getDate())
            .name("text").value(request.getFio())
            .endObject();
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: удаление заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/delReq")
    public ResponseEntity<Void> delReq(@RequestBody DataRequest body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[DELETE] /delReq");
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        requestRepository.delete(request);

        wrtr.name("id").value(request.getId());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("delReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение заголовка заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chTitle")
    public ResponseEntity<Void> chTitle(@RequestBody DataRequest body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chTitle");
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setEmail(body.title);
        requestRepository.saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("title").value(request.getEmail());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chTitle", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение даты заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chDate")
    public ResponseEntity<Void> chDate(@RequestBody DataRequest body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chDate");
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setDate(body.date);
        requestRepository.saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("date").value(request.getDate());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chDate", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение текста заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chText")
    public ResponseEntity<Void> chText(@RequestBody DataRequest body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chText");
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setFio(body.text);
        requestRepository.saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("text").value(request.getFio());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chText", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: [start] отправляет инфу о заявках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getRequests")
    public ResponseEntity<JsonObject> getRequests(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getRequests");
        for(Request reqR : dbService.getRequests()){
            wrtr.name(reqR.getId()+"").beginObject()
                .name("title").value(reqR.getEmail())
                .name("date").value(reqR.getDate())
                .name("text").value(reqR.getFio())
                .endObject();
        }
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые RequestController в методах
     * @see RequestController */
    @ToString
    @RequiredArgsConstructor
    static final class DataRequest {
        public final String text, date, title, email, fio;
        public final Long id;
    }
}