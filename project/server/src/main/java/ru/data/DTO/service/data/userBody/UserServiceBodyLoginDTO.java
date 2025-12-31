package ru.data.DTO.service.data.userBody;

import ru.data.DTO.service.data.UserServiceBodyDTO;

public record UserServiceBodyLoginDTO(String oLogin, String nLogin) implements UserServiceBodyDTO {
}
