package ru.data.DTO.service.data.userBody;

import ru.data.DTO.service.data.UserServiceBodyDTO;

public record UserServiceBodyEmailDTO(String email, Integer role) implements UserServiceBodyDTO {}
