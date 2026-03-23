package ru.controllers.SSE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.data.DTO.SubscriberDTO;
import ru.services.interfaces.logic.ISSEService;

import java.io.IOException;

/** RU: Контроллер для управления Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/SSEController">http://localhost:9001/swagger/htmlSwag/#/SSEController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/sse")
@RequiredArgsConstructor
@RestController
public class SSEController {
    private final ISSEService sseService;

    /** RU: [start] открытие Server Sent Events для нового клиента
     * или сохранение подписки для старого пользователя
     * @param uuidAuth Авторизация, в ней подписка и пользователь
     * @exception IOException Исключение вызывается при ошибках с Json */
    @GetMapping(value = {"/start/{uuidAuth}", "/start"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter start(@PathVariable(required = false) String uuidAuth) throws IOException {
        log.trace("YT3 " + SecurityContextHolder.getContext().getAuthentication());
        return sseService.startSSE(uuidAuth);
    }
}