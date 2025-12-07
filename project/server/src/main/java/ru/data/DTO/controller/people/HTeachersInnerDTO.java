package ru.data.DTO.controller.people;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.people.HTeachersController;

/**
 * RU: Данные клиента используемые HTeachersController в методах
 *
 * @see HTeachersController
 */
@ToString
@RequiredArgsConstructor
public final class HTeachersInnerDTO {
    public final String name;
    public final Long schId, yo, id, grId;
}
