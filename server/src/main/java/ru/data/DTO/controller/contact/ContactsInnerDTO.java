package ru.data.DTO.controller.contact;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.ContactController;

/**
 * RU: Данные клиента используемые ContactsController в методах
 *
 * @see ContactController
 */
@ToString
@RequiredArgsConstructor
public final class ContactsInnerDTO {
    public final String p, p1, val;
}
