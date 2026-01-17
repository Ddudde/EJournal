package ru.services.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.configs.AppConfig;
import ru.controllers.SSE.SSEController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.GetSubscriberDTO;
import ru.services.interfaces.logic.ISSEService;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** RU: сервис для контроллера
 * @see SSEController*/
@Slf4j
@Service
public class SSEService implements ISSEService {

    /** RU: Глобальные подписки, для авторизации и Server Sent Events*/
    public static final Map<UUID, SubscriberDTO> subscriptions = new ConcurrentHashMap<>();

    public SSEService() {
        if(AppConfig.TEST) {
            final UUID key = UUID.fromString(AppConfig.TEST_BEARER_TOKEN);
            SSEService.subscriptions.put(key, new SubscriberDTO(AppConfig.TEST_LOGIN));
        }
    }

    @Override
    public SseEmitter startSSE(String uuidAuth) throws IOException {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final GetSubscriberDTO getSubscriberDTO = getSubscriber(uuidAuth, emitter);
        setSSE(getSubscriberDTO.subscriber, emitter, getSubscriberDTO.uuid);
        emitter.send(SseEmitter.event().name("chck")
            .data(getSubscriberDTO.uuid));
        return emitter;
    }

    private GetSubscriberDTO getSubscriber(String uuidAuth, SseEmitter emitter) {
        UUID uuid;
        SubscriberDTO subscriber;
        if (uuidAuth == null || uuidAuth.equals("null")) {
            uuid = UUID.randomUUID();
            subscriber = new SubscriberDTO(emitter);
            subscriptions.put(uuid, subscriber);
            log.debug("create subscription for " + uuid);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        uuid = UUID.fromString(uuidAuth);
        subscriber = subscriptions.get(uuid);
        if (subscriber != null && subscriber.getLogin() != null) {
            log.debug("subscriptionL save " + uuidAuth);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        if (subscriber != null) {
            subscriptions.remove(uuid);
        }
        uuid = UUID.randomUUID();
        subscriber = new SubscriberDTO(emitter);
        subscriptions.put(uuid, subscriber);
        log.debug("subscriptionNL change to " + uuid);
        return new GetSubscriberDTO(uuid, subscriber);
    }

    /** RU: отправляет всем подходящим клиентам информацию по определённому ивенту
     * @param evName Название ивента
     * @param data Обычно JsonObject или текст
     * @param type Обозначает название раздела в котором находится клиент
     * @param lvlGr "main" обозначает любое значение
     * @see SubscriberDTO */
    @Override
    public void sendEventFor(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        var event = SseEmitter.event().name(evName).data(data);
        subscriptions.forEach((uuid, subscriber) -> {
            if (!subscriber.isSSEComplete()
                    && (type == TypesConnect.MAIN || Objects.equals(type, subscriber.getType()))
                    && (Objects.equals(lvlSch, "main") || Objects.equals(lvlSch, subscriber.getLvlSch()))
                    && (Objects.equals(lvlGr, "main") || Objects.equals(lvlGr, subscriber.getLvlGr()))
                    && (Objects.equals(lvlMore1, "main") || Objects.equals(lvlMore1, subscriber.getLvlMore1()))
                    && (Objects.equals(lvlMore2, "main") || Objects.equals(lvlMore2, subscriber.getLvlMore2()))) {
                try {
                    subscriber.getSSE().send(event);
                } catch (IOException e) {
                    if(subscriber.getLogin() == null) {
                        subscriptions.remove(uuid);
                        log.debug("subscription " + uuid + " was closed from Ping or Error");
                    } else {
                        log.debug("subscription " + uuid + " was noclosed from Ping or Error " + subscriber.getLogin());
                    }
                    subscriber.getSSE().complete();
                }
            }
        });
    }

    /** RU: изменение подписки
     * Все параметры, являются свойствами подписки*/
    @Override
    public void changeSubscriber(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid == null) return;
        final SubscriberDTO sub = subscriptions.get(UUID.fromString(uuid));
        if(sub == null) return;
        if(login != null) {
            sub.setLogin(login);
            log.trace("setLog " + login + " subscription for " + uuid);
        }
        if(type != null) {
            sub.setType(type);
            log.trace("setType " + type + " subscription for " + uuid);
        }
        if(lvlSch != null) {
            sub.setLvlSch(lvlSch);
            log.trace("setLvlSch " + lvlSch + " subscription for " + uuid);
        }
        if(lvlGr != null) {
            sub.setLvlGr(lvlGr);
            log.trace("setLvlGr " + lvlGr + " subscription for " + uuid);
        }
        if(lvlMore1 != null) {
            sub.setLvlMore1(lvlMore1);
            log.trace("setLvlMore1 " + lvlMore1 + " subscription for " + uuid);
        }
        if(lvlMore2 != null) {
            sub.setLvlMore2(lvlMore2);
            log.trace("setLvlMore2 " + lvlMore2 + " subscription for " + uuid);
        }
    }

    /** RU: изменияет Server Sent Events пользователя и устанавливает ему UUID
     * @see SSEController#start(String)   */
    @Override
    public void setSSE(SubscriberDTO subscriber, SseEmitter SSE, UUID uuid) {
        System.out.println("yes: " + subscriber);
        subscriber.setSSE(SSE);
        if(SSE == null) return;

        subscriber.setSSEComplete(false);
        SSE.onCompletion(() -> {
            onCloseSSE(subscriber, uuid, "onCompletion");
        });
        SSE.onTimeout(() -> {
            SSE.complete();
            onCloseSSE(subscriber, uuid, "onTimeout");
        });
    }

    private void onCloseSSE(SubscriberDTO subscriber, UUID uuid, String reason) {
        if(subscriber.getLogin() == null) {
            subscriptions.remove(uuid);
            log.debug("subscription " + uuid + " was closed from " + reason);
        } else {
            subscriber.setSSEComplete(true);
            log.debug("subscription " + uuid + " was noclosed from " + reason + " " + subscriber.getLogin());
        }
    }
}
