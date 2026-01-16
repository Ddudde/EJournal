package ru.data.DTO.controller.school.analytics.period;

import lombok.Builder;

import java.util.Map;

@Builder
public record PeriodOutDTO(Long id, PeriodOutBodyDTO body, Map<Integer, PeriodOutBodyDTO> bodyP) {
}
