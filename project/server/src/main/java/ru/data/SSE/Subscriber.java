package ru.data.SSE;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.controllers.SSEController;
import ru.data.DAO.auth.User;

import java.util.UUID;

import static ru.Main.datas;

/** RU: Подписка, используется для Server Sent Events и иногда в других местах
 * <pre>
 * </pre> */
@Slf4j
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@ToString public class Subscriber {
    private String login;

    /** RU: Тип подключения, где находится клиент */
    private TypesConnect type;

    /** RU: Школа пользователя */
    private String lvlSch;

    /** RU: Группа пользователя */
    private String lvlGr;

    /** RU: Чаще используется для роли пользователя */
    private String lvlMore1;

    /** RU: Дополнительный уровень пользователя */
    private String lvlMore2;

    /** RU: Server Sent Events пользователя */
    private SseEmitter SSE;

    private boolean SSEComplete = false;

    public Subscriber(SseEmitter SSE) {
        this.SSE = SSE;
    }

    public Subscriber(String login) {
        this.login = login;
    }

    /** RU: изменияет Server Sent Events пользователя и устанавливает ему UUID
     * @see SSEController#start(String)   */
    public void setSSE(SseEmitter SSE, UUID uuid) {
        this.SSE = SSE;
        if(SSE == null) return;
        SSEComplete = false;
        SSE.onCompletion(() -> {
            if(login == null) {
                datas.subscriptions.remove(uuid);
                log.debug("subscription " + uuid + " was closed from onCompletion");
            } else {
                SSEComplete = true;
                log.debug("subscription " + uuid + " was noclosed from onCompletion " + login);
            }
        });
        SSE.onTimeout(() -> {
            SSE.complete();
            if(login == null) {
                datas.subscriptions.remove(uuid);
                log.debug("subscription " + uuid + " was closed from onTimeout");
            } else {
                SSEComplete = true;
                log.debug("subscription " + uuid + " was noclosed from onTimeout " + login);
            }
        });
    }

    public User getUser() {
        return datas.getDbService().userByLogin(login);
    }
}