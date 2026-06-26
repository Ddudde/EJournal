package ru.data.DTO.service.data.userBody;

import lombok.Builder;

@Builder
public record UserServiceBodyUserDTO(String name, String login, String link) {
}
