package ru.data.DTO.controller.main.profile;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProfileOutDTO(Long kid, Integer role, Map<String, String> kids, String login, Integer ico, Long id,
    String fio, String more, Map<Integer, ProfileOutBodyRolesDTO> roles) {}