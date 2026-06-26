package ru.data.DTO.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.AuthController;
import ru.controllers.SSE.TypesConnect;

/**
 * RU: Данные клиента используемые AuthController в методах
 *
 * @see AuthController
 */
@ToString
@RequiredArgsConstructor
public final class AuthInnerDTO {
    public final TypesConnect type;
    public final String code, notifToken, login, secFr, par, mod, id;
    public final int ico;
    public final boolean permis;
}
