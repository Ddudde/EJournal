package ru.data.DTO.controller.school.dnevnik;

import java.util.Map;

public record DnevnikBodyDateDTO(Map<Integer, DnevnikBodyMarkDTO> marks, Integer i) {
}
