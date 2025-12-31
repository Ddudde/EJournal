package ru.data.DTO.service.data.initDB;

import lombok.Builder;

import java.util.Map;

@Builder
public record InitDBServiceBodySchoolDTO(String name, Map<Long, InitDBServiceBodyDTO> hteachers,
     Map<Long, InitDBServiceBodyDTO> teachers, Map<Long, InitDBServiceBodyGroupDTO> groups) {
}
