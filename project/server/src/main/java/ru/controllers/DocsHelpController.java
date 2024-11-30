package ru.controllers;

import org.springframework.http.ResponseEntity;

import java.io.IOException;

/** RU: JavaDoc для контроллеров */
public interface DocsHelpController {

    /** RU: Описание для эндпоинта с авторизацией
     * @param body Данные с клиента
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception IOException Исключение вызывается при ошибках с Json
     * @return Объект и/или код статуса */
    ResponseEntity point(Object body, Object auth) throws IOException;
}
