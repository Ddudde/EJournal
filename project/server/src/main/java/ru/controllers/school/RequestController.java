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
import ru.data.DAO.school.Request;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;

import static ru.Main.datas;

/** RU: Контроллер для раздела заявок на подключение к системе учебных учреждений + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/RequestController">http://localhost:9001/swagger/htmlSwag/#/RequestController</a>
 * </pre>
 * @see Subscriber */
@RequestMapping("/requests")
@RequiredArgsConstructor
@RestController public class RequestController {

    /** RU: добавляет заявку + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PostMapping("/addReq")
    public ResponseEntity<Void> addReq(@RequestBody DataRequest body) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addReq");
        if(ObjectUtils.isEmpty(body.email) || ObjectUtils.isEmpty(body.date) || ObjectUtils.isEmpty(body.fio)) {
            return ResponseEntity.notFound().build();
        }
        final Request request = new Request(body.email, body.date, body.fio);
        datas.getDbService().getRequestRepository().saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("body").beginObject()
            .name("title").value(request.getEmail())
            .name("date").value(request.getDate())
            .name("text").value(request.getFio())
            .endObject();
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("addReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: удаление заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/delReq")
    public ResponseEntity<Void> delReq(@RequestBody DataRequest body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /delReq");
        final Request request = datas.getDbService().requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        datas.getDbService().getRequestRepository().delete(request);

        wrtr.name("id").value(request.getId());
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("delReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение заголовка заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chTitle")
    public ResponseEntity<Void> chTitle(@RequestBody DataRequest body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chTitle");
        final Request request = datas.getDbService().requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setEmail(body.title);
        datas.getDbService().getRequestRepository().saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("title").value(request.getEmail());
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chTitle", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение даты заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chDate")
    public ResponseEntity<Void> chDate(@RequestBody DataRequest body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chDate");
        final Request request = datas.getDbService().requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setDate(body.date);
        datas.getDbService().getRequestRepository().saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("date").value(request.getDate());
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chDate", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменение текста заявки + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chText")
    public ResponseEntity<Void> chText(@RequestBody DataRequest body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chText");
        final Request request = datas.getDbService().requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        request.setFio(body.text);
        datas.getDbService().getRequestRepository().saveAndFlush(request);

        wrtr.name("id").value(request.getId())
            .name("text").value(request.getFio());
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chText", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: [start] отправляет инфу о заявках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @GetMapping("/getRequests")
    public ResponseEntity<JsonObject> getRequests(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getRequests");
        for(Request reqR : datas.getDbService().getRequests()){
            wrtr.name(reqR.getId()+"").beginObject()
                .name("title").value(reqR.getEmail())
                .name("date").value(reqR.getDate())
                .name("text").value(reqR.getFio())
                .endObject();
        }
        return datas.getObjR(ans -> {
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