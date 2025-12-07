package ru.data.DTO.controller.school;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.school.TeacherJournalController;

/**
 * RU: Данные клиента используемые PJournalController в методах
 *
 * @see TeacherJournalController
 */
@ToString
@RequiredArgsConstructor
public final class TeacherJournalInnerDTO {
    public final String style, day, mark, homework;
    public final Long group, kid, per;
    public final int weight;
}
