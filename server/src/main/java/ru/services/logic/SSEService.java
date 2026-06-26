package ru.services.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.configs.AppConfig;
import ru.controllers.SSE.SSEController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.GetSubscriberDTO;
import ru.data.DTO.service.ConnectDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.logic.ISSEService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** RU: сервис для контроллера
 * @see SSEController*/
@Slf4j
@Service
public class SSEService implements ISSEService {
    /** RU: Глобальные подписки, для связи клиентов и Server Sent Events*/
    public static final Map<UUID, SubscriberDTO> subscriptions = new ConcurrentHashMap<>();

    /** RU: Одноразовые токены для подключения SSE */
    public static final Map<UUID, ConnectDTO> connectTokens = new ConcurrentHashMap<>();
    /** RU: 10 минут */
    private final int TIME_OF_CHECK_TOKENS = 60000 * 10;
    /** RU: 15 минут */
    private final int TIME_OF_LIVE_TOKENS = 60000 * 15;
    private final Timer timer = new Timer();

    public SSEService() {
        if(AppConfig.TEST) {
            final UUID key = UUID.fromString(AppConfig.TEST_SSE_TOKEN);
            SSEService.subscriptions.put(key, new SubscriberDTO());
        }
        timer.scheduleAtFixedRate(new TimerSSE(), 0, TIME_OF_CHECK_TOKENS);
    }

    @Override
    public SseEmitter startSSE(String uuidAuth, Long userId) throws IOException {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final GetSubscriberDTO getSubscriberDTO = getSubscriber(uuidAuth, emitter, userId);
        setSSE(getSubscriberDTO.subscriber, emitter, getSubscriberDTO.uuid, userId);
        emitter.send(SseEmitter.event().name("chck")
            .data(getSubscriberDTO.uuid));
        return emitter;
    }

    private GetSubscriberDTO getSubscriber(String uuidAuth, SseEmitter emitter, Long userId) {
        UUID uuid;
        SubscriberDTO subscriber;
        final long now = System.currentTimeMillis();
        if (uuidAuth == null || uuidAuth.equals("null")) {
            uuid = UUID.randomUUID();
            subscriber = new SubscriberDTO(emitter, now + TIME_OF_LIVE_TOKENS);
            subscriptions.put(uuid, subscriber);
            log.debug("create subscription for " + uuid);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        uuid = UUID.fromString(uuidAuth);
        subscriber = subscriptions.get(uuid);
        if (subscriber != null && userId != null) {
            log.debug("subscriptionL save " + uuidAuth);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        if (subscriber != null) {
            subscriptions.remove(uuid);
        }
        uuid = UUID.randomUUID();
        subscriber = new SubscriberDTO(emitter, now + TIME_OF_LIVE_TOKENS);
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
    public void sendEventFor(Long userId, String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        final SseEmitter.SseEventBuilder event = SseEmitter.event().name(evName).data(data);
        subscriptions.forEach((uuid, subscriberDTO) -> {
            if (!filterSSE(type, lvlSch, lvlGr, lvlMore1, lvlMore2, subscriberDTO)) return;

            try {
                subscriberDTO.getSSE().send(event);
            } catch (IOException e) {
                onCloseSSE(subscriberDTO, uuid, "Ping or Error", userId);
            }
        });
    }

    private boolean filterSSE(TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2, SubscriberDTO subscriberDTO) {
        return !subscriberDTO.isSSEComplete()
            && (type == TypesConnect.MAIN || Objects.equals(type, subscriberDTO.getType()))
            && (Objects.equals(lvlSch, "main") || Objects.equals(lvlSch, subscriberDTO.getLvlSch()))
            && (Objects.equals(lvlGr, "main") || Objects.equals(lvlGr, subscriberDTO.getLvlGr()))
            && (Objects.equals(lvlMore1, "main") || Objects.equals(lvlMore1, subscriberDTO.getLvlMore1()))
            && (Objects.equals(lvlMore2, "main") || Objects.equals(lvlMore2, subscriberDTO.getLvlMore2()));
    }

    /** RU: изменение подписки
     * Все параметры, являются свойствами подписки*/
    @Override
    public void changeSubscriber(String uuid, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid == null) return;
        final SubscriberDTO sub = subscriptions.get(UUID.fromString(uuid));
        if(sub == null) return;

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
     * @see SSEController#start(AuthToken)   */
    @Override
    public void setSSE(SubscriberDTO subscriberDTO, SseEmitter SSE, UUID uuid, Long userId) {
        subscriberDTO.setSSE(SSE);
        if(SSE == null) return;

        subscriberDTO.setSSEComplete(false);
        SSE.onCompletion(() -> {
            onCloseSSE(subscriberDTO, uuid, "onCompletion", userId);
        });
        SSE.onTimeout(() -> {
            SSE.complete();
            onCloseSSE(subscriberDTO, uuid, "onTimeout", userId);
        });
    }

    @Override
    public void onCloseSSE(SubscriberDTO subscriberDTO, UUID uuid, String reason, Long userId) {
        if(userId == null && !subscriberDTO.isSSEComplete()) {
            subscriptions.remove(uuid);
            log.debug("subscription " + uuid + " was closed from " + reason);
        } else {
            log.debug("subscription " + uuid + " was noclosed from " + reason + " " + userId);
        }
        subscriberDTO.setSSEComplete(true);
        subscriberDTO.getSSE().complete();
    }

    @Override
    public UUID getOneTimeToken(AuthToken auth) {
        final UUID newConnect = UUID.randomUUID();
        final UUID subscriberToken = Optional.ofNullable(auth.getUUID())
            .map(UUID::fromString)
            .orElse(null);
        final ConnectDTO connectDTO = new ConnectDTO(subscriberToken, auth.getUserId());
        SSEService.connectTokens.put(newConnect, connectDTO);
        return newConnect;
    }
}
