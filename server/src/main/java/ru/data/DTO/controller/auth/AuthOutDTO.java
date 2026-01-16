package ru.data.DTO.controller.auth;

import lombok.Builder;

import java.util.Map;

@Builder
public record AuthOutDTO(Integer role, Long kid, Map<Long, String> kids, Boolean auth, String login, String uuidS,
     Integer ico, Boolean roles, Boolean secFr, Boolean email, String error, Long id, Long id1, String code) {
}
