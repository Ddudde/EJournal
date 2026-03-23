package ru.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.controllers.SSE.TypesConnect;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.test.TestInnerDTO;
import ru.data.DTO.controller.test.TestOutDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.ITestService;

/** RU: Контроллер для раздела тестирования
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TestController">http://localhost:9001/swagger/htmlSwag/#/TestController</a>
 * </pre> */
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController public class TestController {
    private final ITestService testService;
    private final ISSEService sseService;

    /** RU: изменяет параметры тестирования
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PutMapping("/chTests")
    public ResponseEntity<TestOutDTO> chTests(@RequestBody TestInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final TestOutDTO outDTO = testService.changeTests(body);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] отправка инфы для тестов
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<TestOutDTO> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {

        final TestOutDTO outDTO = testService.prepareInfo();
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.TEST, "main", "main", "main", "main");
        return ResponseEntity.ok(outDTO);
    }
}