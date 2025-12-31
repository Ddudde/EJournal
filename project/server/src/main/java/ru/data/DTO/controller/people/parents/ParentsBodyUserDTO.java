package ru.data.DTO.controller.people.parents;

import lombok.Builder;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.Map;

@Builder
public record ParentsBodyUserDTO(String name, Map<Long, UserServiceBodyUserDTO> par, String login, String link) {
}
