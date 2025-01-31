package ru.data.DTO;

import lombok.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.controllers.TypesConnect;

/** RU: Подписка, используется для Server Sent Events и иногда в других местах */
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@ToString public class SubscriberDTO {
    private Long userId;
    private String login;
    private boolean SSEComplete = false;

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

    public SubscriberDTO(SseEmitter SSE) {
        this.SSE = SSE;
    }

    public SubscriberDTO(String login) {
        this.login = login;
    }

}