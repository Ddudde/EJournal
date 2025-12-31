package ru.data.DTO.controller.school.request;

import lombok.Builder;

@Builder
public record RequestBodyDTO(String title, String date, String text) {
}
