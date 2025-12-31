package ru.data.DTO.service.data.initDB;

import lombok.Builder;

import java.util.Map;

@Builder
public record InitDBServiceBodyDTO(String fio, String login, String code, String testPassword,
    Map<Long, InitDBServiceBodyDTO> admins, Map<Long, InitDBServiceBodySchoolDTO> schools) {
}
