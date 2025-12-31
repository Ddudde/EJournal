package ru.data.DTO.controller.people.parents;

import lombok.Builder;

import java.util.Map;

@Builder
public record ParentsBodyDTO(String name, Map<String, ParentsBodyDTO> par, String login, String link) {
    public ParentsBodyDTO(String name) {
        this(name, null, null, null);
    }
}
