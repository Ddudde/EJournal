package ru.services.interfaces.logic;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.controllers.SSE.TypesConnect;
import ru.data.DTO.SubscriberDTO;

import java.io.IOException;
import java.util.UUID;

public interface ISSEService {
    SseEmitter startSSE(String uuidAuth) throws IOException;

    void sendEventFor(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2);

    void changeSubscriber(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2);

    void setSSE(SubscriberDTO subscriber, SseEmitter SSE, UUID uuid);
}
