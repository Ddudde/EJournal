package ru.data.DTO.controller.people.hTeacher;

import lombok.Builder;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.Map;

@Builder
public record HTeachersBodyDTO(String name, Map<Long, UserServiceBodyUserDTO> pep) {
    public HTeachersBodyDTO(String name) {
        this(name, null);
    }
}
