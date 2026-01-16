package ru.data.DTO.controller.test.body;

import lombok.Builder;

import java.util.Map;

@Builder
public record TestServiceBodySchoolDTO(String name, Map<Long, TestServiceBodyDTO> hteachers,
                                       Map<Long, TestServiceBodyDTO> teachers, Map<Long, TestServiceBodyGroupDTO> groups) {
}
