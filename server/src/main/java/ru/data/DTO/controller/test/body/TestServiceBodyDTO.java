package ru.data.DTO.controller.test.body;

import lombok.Builder;

import java.util.Map;

@Builder
public record TestServiceBodyDTO(String fio, String login, String code, String testPassword,
     Map<Long, TestServiceBodyDTO> admins, Map<Long, TestServiceBodySchoolDTO> schools) {
}
