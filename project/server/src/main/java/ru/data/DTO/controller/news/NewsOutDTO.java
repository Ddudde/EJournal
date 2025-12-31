package ru.data.DTO.controller.news;

import lombok.Builder;

@Builder
public record NewsOutDTO(Long id, String type, String val, NewsOutBodyDTO body) {
}
