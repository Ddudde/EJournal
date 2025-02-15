package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.IniDBService;

/** RU: Контроллер для раздела тестирования
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TestController">http://localhost:9001/swagger/htmlSwag/#/TestController</a>
 * </pre> */
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController public class TestController {
    private final IniDBService iniDBService;
    private final MainService mainService;

    /** RU: изменяет параметры тестирования
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PutMapping("/chTests")
    public ResponseEntity<JsonObject> chTests(@RequestBody DataTest body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PUT] /chTests");
        switch (body.id) {
            case "checkbox_debug" -> MainService.debug = body.val;
            case "checkbox_test" -> {
                MainService.test = body.val;
                if(MainService.test) {
                    iniDBService.testOn();
                } else {
                    iniDBService.testOff();
                }
                iniDBService.getTestInfo(wrtr);
            }
            default -> {}
        }
        return mainService.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправка инфы для тестов
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfo");
        wrtr.name("bodyS").beginObject()
            .name("checkbox_debug").value(MainService.debug)
            .name("checkbox_test").value(MainService.test)
            .endObject();
        iniDBService.getTestInfo(wrtr);
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.TEST, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые TestController в методах
     * @see TestController */
    @ToString
    @RequiredArgsConstructor
    static final class DataTest {
        public final String id;
        public final boolean val;
    }
}