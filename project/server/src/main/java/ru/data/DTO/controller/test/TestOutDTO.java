package ru.data.DTO.controller.test;

import lombok.Builder;
import ru.data.DTO.service.data.initDB.InitDBServiceBodyDTO;

@Builder
public record TestOutDTO(InitDBServiceBodyDTO bodyT, TestBodyDTO bodyS) {
}
