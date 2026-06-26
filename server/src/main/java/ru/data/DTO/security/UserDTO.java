package ru.data.DTO.security;

import java.util.List;

public record UserDTO(Long id, List<String> roles, String JWTToken) {
}
