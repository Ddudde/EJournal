package ru.data.DTO.controller.main;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.main.SettingsController;

/**
 * RU: Данные клиента используемые SettingsController в методах
 *
 * @see SettingsController
 */
@ToString
@RequiredArgsConstructor
public final class SettingsInnerDTO {
    public final String secFR, nPar, id, email, emailCode, login,
            notifToken, invCod, valString;
    public final int valInt;
    public final boolean val, emailSt;
}
