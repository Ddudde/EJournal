package ru.data.DTO.controller.news;

import lombok.Builder;

@Builder
public record NewsOutBodyDTO(String title, String date, String img_url, String text) {
}
