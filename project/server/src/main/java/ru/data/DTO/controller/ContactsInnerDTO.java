package ru.data.DTO.controller;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.ContactsController;

/**
 * RU: Данные клиента используемые ContactsController в методах
 *
 * @see ContactsController
 */
@ToString
@RequiredArgsConstructor
public final class ContactsInnerDTO {
    public final String p, p1, val;
}
