package ru.data.DTO.controller.people;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.people.AdminsController;

/**
 * RU: Данные клиента используемые AdminsController в методах
 *
 * @see AdminsController
 */
@ToString
@RequiredArgsConstructor
public final class AdminsInnerDTO {
    public final String name;
    public final Long id;
}
