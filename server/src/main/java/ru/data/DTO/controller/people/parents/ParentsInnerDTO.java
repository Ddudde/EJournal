package ru.data.DTO.controller.people.parents;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.people.ParentsController;

/**
 * RU: Данные клиента используемые ParentsController в методах
 *
 * @see ParentsController
 */
@ToString
@RequiredArgsConstructor
public final class ParentsInnerDTO {
    public final String name;
    public final Long id;
    public final ParentsBodyDTO bod;
}
