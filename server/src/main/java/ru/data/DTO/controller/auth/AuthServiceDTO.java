package ru.data.DTO.controller.auth;

import lombok.Builder;

import java.util.Map;

@Builder
public record AuthServiceDTO(Long kid, Map<Long, String> kids) {
}
