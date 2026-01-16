package ru.data.DTO.controller.school.dnevnik;

import lombok.Builder;

@Builder
public record DnevnikBodyLessonDTO(String name, String cabinet, String group, DnevnikBodyDTO prepod) {
}
