package ru.data.DTO.controller.main.profile;

import lombok.Builder;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.Map;
import java.util.Set;

@Builder
public record ProfileOutBodyRolesDTO(String email, String yo, String group, Set<String> lessons,
     Map<Long, UserServiceBodyUserDTO> kids, Map<Long, UserServiceBodyUserDTO> parents){
}
