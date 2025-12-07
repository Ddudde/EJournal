package ru.data.DTO.controller.main;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.main.ProfileController;

/**
 * RU: Данные клиента используемые ProfileController в методах
 *
 * @see ProfileController
 */
@ToString
@RequiredArgsConstructor
public final class ProfileInnerDTO {
    public final String nLogin, info, email, notifToken;
    public final Long idL;
}
