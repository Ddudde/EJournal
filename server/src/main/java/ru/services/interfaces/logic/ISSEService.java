package ru.services.interfaces.logic;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.controllers.SSE.TypesConnect;
import ru.data.DTO.SubscriberDTO;
import ru.security.user.AuthToken;

import java.io.IOException;
import java.util.UUID;

public interface ISSEService {
    SseEmitter startSSE(String uuidAuth, Long userId) throws IOException;

    void sendEventFor(Long userId, String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2);

    void changeSubscriber(String uuid, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2);

    void setSSE(SubscriberDTO subscriber, SseEmitter SSE, UUID uuid, Long userId);

    void onCloseSSE(SubscriberDTO subscriberDTO, UUID uuid, String reason, Long userId);

    UUID getOneTimeToken(AuthToken auth);
}
