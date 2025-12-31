package ru.controllers.school;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.school.Request;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.request.RequestBodyDTO;
import ru.data.DTO.controller.school.request.RequestInnerDTO;
import ru.data.DTO.controller.school.request.RequestOutDTO;
import ru.security.user.CustomToken;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.school.IRequestService;

import java.util.List;
import java.util.Map;

/** RU: Контроллер для раздела заявок на подключение к системе учебных учреждений + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/RequestController">http://localhost:9001/swagger/htmlSwag/#/RequestController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/requests")
@RequiredArgsConstructor
@RestController public class RequestController {
    private final IDBService dbService;
    private final IRequestService requestService;
    private final ISSEService sseService;

    /** RU: добавляет заявку + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PostMapping("/addReq")
    public ResponseEntity<Void> addReq(@RequestBody RequestInnerDTO body) {
        if(ObjectUtils.isEmpty(body.email) || ObjectUtils.isEmpty(body.date) || ObjectUtils.isEmpty(body.fio)) {
            return ResponseEntity.notFound().build();
        }

        final RequestOutDTO outDTO = requestService.addRequest(body);
        sseService.sendEventFor("addReq", outDTO, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: удаление заявки + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/delReq")
    public ResponseEntity<Void> delReq(@RequestBody RequestInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        final RequestOutDTO outDTO = requestService.deleteRequest(request);
        sseService.sendEventFor("delReq", outDTO, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменение заголовка заявки + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chTitle")
    public ResponseEntity<Void> chTitle(@RequestBody RequestInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        final RequestOutDTO outDTO = requestService.changeTitle(body.title, request);
        sseService.sendEventFor("chTitle", outDTO, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменение даты заявки + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chDate")
    public ResponseEntity<Void> chDate(@RequestBody RequestInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        final RequestOutDTO outDTO = requestService.changeDate(body.date, request);
        sseService.sendEventFor("chDate", outDTO, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменение текста заявки + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chText")
    public ResponseEntity<Void> chText(@RequestBody RequestInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Request request = dbService.requestById(body.id);
        if(request == null) return ResponseEntity.notFound().build();

        final RequestOutDTO outDTO = requestService.changeText(body.text, request);
        sseService.sendEventFor("chText", outDTO, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет инфу о заявках
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getRequests")
    public ResponseEntity<Map<Long, RequestBodyDTO>> getRequests(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final List<Request> requests = dbService.getRequests();

        final Map<Long, RequestBodyDTO> outDTO = requestService.prepareRequests(requests);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.REQUESTS, "main", "main", "main", "main");
        return ResponseEntity.ok(outDTO);
    }

}