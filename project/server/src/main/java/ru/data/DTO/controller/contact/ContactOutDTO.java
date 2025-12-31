package ru.data.DTO.controller.contact;

import lombok.Builder;

@Builder
public record ContactOutDTO(String val, String p, String p1, String contact, ContactBodyDTO mapPr) {
}
