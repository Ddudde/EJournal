package ru.data.DTO.controller.test.body;

import lombok.Builder;

import java.util.Map;

@Builder
public record TestServiceBodyGroupDTO(String name, Map<Long, TestServiceBodyDTO> kids,
                                      Map<Long, TestServiceBodyDTO> parents) {
}
