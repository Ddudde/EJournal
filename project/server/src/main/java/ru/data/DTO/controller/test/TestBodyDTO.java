package ru.data.DTO.controller.test;

import lombok.Builder;

@Builder
public record TestBodyDTO(Boolean checkbox_debug, Boolean checkbox_test) {
}
