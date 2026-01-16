package ru.data.DTO.controller.people.parents;

import lombok.Builder;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.Map;

@Builder
public record ParentsOutDTO(Long id, String name, ParentsBodyDTO body, Map<Long, UserServiceBodyUserDTO> bodyC,
    Map<Long, ParentsBodyUserDTO> bodyP) {
}
