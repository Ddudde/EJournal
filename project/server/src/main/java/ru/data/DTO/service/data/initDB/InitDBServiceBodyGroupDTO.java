package ru.data.DTO.service.data.initDB;

import lombok.Builder;

import java.util.Map;

@Builder
public record InitDBServiceBodyGroupDTO(String name, Map<Long, InitDBServiceBodyDTO> kids,
    Map<Long, InitDBServiceBodyDTO> parents) {
}
