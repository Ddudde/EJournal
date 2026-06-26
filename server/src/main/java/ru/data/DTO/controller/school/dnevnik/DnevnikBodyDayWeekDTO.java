package ru.data.DTO.controller.school.dnevnik;

import java.util.Map;

public record DnevnikBodyDayWeekDTO(Map<Integer, DnevnikBodyLessonDTO> lessons) {
}
