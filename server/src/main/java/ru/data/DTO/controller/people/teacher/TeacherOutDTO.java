package ru.data.DTO.controller.people.teacher;

import lombok.Builder;

@Builder
public record TeacherOutDTO(Long id, String name) {
    public TeacherOutDTO(Long id) {
        this(id, null);
    }
}
