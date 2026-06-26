package ru.data.DTO.controller.people.teacher;

import lombok.Builder;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.Map;

@Builder
public record TeacherOutBodyDTO(Map<Long, UserServiceBodyUserDTO> tea, String name) {
    public TeacherOutBodyDTO(Map<Long, UserServiceBodyUserDTO> tea){
        this(tea, null);
    }
}
