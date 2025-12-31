package ru.data.DTO.controller.people.admin;

import lombok.Builder;

@Builder
public record AdminsOutDTO(Long id, String name, AdminsOutBodyDTO body) {
}
