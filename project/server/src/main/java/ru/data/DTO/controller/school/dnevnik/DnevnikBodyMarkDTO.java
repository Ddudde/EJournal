package ru.data.DTO.controller.school.dnevnik;

import lombok.Builder;

@Builder
public record DnevnikBodyMarkDTO(String mark, Integer weight, String type, String homework) {
}
