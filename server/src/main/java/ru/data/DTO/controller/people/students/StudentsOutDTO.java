package ru.data.DTO.controller.people.students;

import lombok.Builder;

@Builder
public record StudentsOutDTO(Long id, String name, StudentsOutBodyDTO body) {
    public StudentsOutDTO(Long id){
        this(id, null, null);
    }
}
