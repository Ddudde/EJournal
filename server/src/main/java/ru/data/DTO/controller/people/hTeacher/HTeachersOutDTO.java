package ru.data.DTO.controller.people.hTeacher;

import lombok.Builder;

@Builder
public record HTeachersOutDTO(Long id, Long id1, String name, HTeachersBodyDTO body) {
}
