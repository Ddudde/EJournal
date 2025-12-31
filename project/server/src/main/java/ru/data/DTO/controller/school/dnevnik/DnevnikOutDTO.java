package ru.data.DTO.controller.school.dnevnik;

import java.util.Map;

public record DnevnikOutDTO(Map<Integer, DnevnikBodyDayWeekDTO> body, String min, String max,
    Map<String, Map<String, DnevnikBodyDateDTO>> bodyD) {
}
