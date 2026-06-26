package ru.data.DTO.service.data;

import lombok.Builder;

import java.util.Map;

@Builder
public record UserServiceDTO (Long kid, Integer role, Map<String, String> kids, UserServiceBodyDTO body){
}
