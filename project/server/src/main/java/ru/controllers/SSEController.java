package ru.controllers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.data.DTO.GetSubscriberDTO;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static ru.Main.datas;

/** RU: Контроллер для управления Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/SSEController">http://localhost:9001/swagger/htmlSwag/#/SSEController</a>
 * </pre>
 * @see Subscriber */
@Slf4j
@RequestMapping("/sse")
@NoArgsConstructor
@RestController
public class SSEController {

    /** RU: [start] открытие Server Sent Events для нового клиента
     * или сохранение подписки для старого пользователя
     * @param uuidAuth Авторизация, в ней подписка и пользователь
     * @exception IOException Исключение вызывается при ошибках с Json */
    @GetMapping(value = {"/start/{uuidAuth}", "/start"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter start(@PathVariable(required = false) String uuidAuth) throws IOException {
        log.trace("YT3 " + SecurityContextHolder.getContext().getAuthentication());
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final GetSubscriberDTO getSubscriberDTO = getSubscriber(uuidAuth, emitter);
        getSubscriberDTO.subscriber.setSSE(emitter, getSubscriberDTO.uuid);
        emitter.send(SseEmitter.event().name("chck")
            .data(getSubscriberDTO.uuid));
        return emitter;
    }

    private GetSubscriberDTO getSubscriber(String uuidAuth, SseEmitter emitter) {
        UUID uuid;
        Subscriber subscriber;
        if (uuidAuth == null || uuidAuth.equals("null")) {
            uuid = UUID.randomUUID();
            subscriber = new Subscriber(emitter);
            datas.subscriptions.put(uuid, subscriber);
            log.debug("create subscription for " + uuid);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        uuid = UUID.fromString(uuidAuth);
        subscriber = datas.subscriptions.get(uuid);
        if (subscriber != null && subscriber.getLogin() != null) {
            log.debug("subscriptionL save " + uuidAuth);
            return new GetSubscriberDTO(uuid, subscriber);
        }
        if (subscriber != null) {
            datas.subscriptions.remove(uuid);
        }
        uuid = UUID.randomUUID();
        subscriber = new Subscriber(emitter);
        datas.subscriptions.put(uuid, subscriber);
        log.debug("subscriptionNL change to " + uuid);
        return new GetSubscriberDTO(uuid, subscriber);
    }

    /** RU: отправляет всем подходящим клиентам информацию по определённому ивенту
     * @param evName Название ивента
     * @param data Обычно JsonObject или текст
     * @param type Обозначает название раздела в котором находится клиент
     * @param lvlGr "main" обозначает любое значение
     * @see Subscriber*/
    public static void sendEventFor(String evName, Object data, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2) {
        var event = SseEmitter.event().name(evName).data(data);
        datas.subscriptions.forEach((uuid, subscriber) -> {
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
                        datas.subscriptions.remove(uuid);
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
    public static void changeSubscriber(String uuid, String login, TypesConnect type, String lvlSch, String lvlGr, String lvlMore1, String lvlMore2){
        if(uuid == null) return;
        final Subscriber sub = datas.subscriptions.get(UUID.fromString(uuid));
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
}