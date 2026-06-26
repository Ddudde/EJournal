package ru.data.DTO.controller.people.students;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.people.StudentsController;

/**
 * RU: Данные клиента используемые StudentsController в методах
 *
 * @see StudentsController
 */
@ToString
@RequiredArgsConstructor
public final class StudentsInnerDTO {
    public final String name;
    public final Long id;
}
