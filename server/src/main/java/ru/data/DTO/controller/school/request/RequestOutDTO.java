package ru.data.DTO.controller.school.request;

import lombok.Builder;

@Builder
public record RequestOutDTO(Long id, RequestBodyDTO body, String title, String date, String text) {
}
