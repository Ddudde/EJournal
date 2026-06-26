package ru.data.DTO.service.school;

import ru.data.DTO.controller.school.dnevnik.DnevnikBodyDayWeekDTO;

import java.util.Map;

public record ScheduleServiceDTO(Map<Integer, DnevnikBodyDayWeekDTO> body) {
}
