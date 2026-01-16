package ru.data.DTO.controller.school.analytics.period;

import lombok.Builder;

@Builder
public record PeriodOutBodyDTO(String name, String perN, String perK) {
}
