package ru.data.DTO.controller.test;

import lombok.Builder;
import ru.data.DTO.controller.test.body.TestBodyDTO;
import ru.data.DTO.controller.test.body.TestServiceBodyDTO;

@Builder
public record TestOutDTO(TestServiceBodyDTO bodyT, TestBodyDTO bodyS) {
    public TestOutDTO(TestServiceBodyDTO bodyT) {
        this(bodyT, null);
    }
}
