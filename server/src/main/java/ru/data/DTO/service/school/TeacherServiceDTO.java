package ru.data.DTO.service.school;

import lombok.Builder;
import ru.data.DTO.controller.people.teacher.TeacherOutBodyDTO;

import java.util.Map;

@Builder
public record TeacherServiceDTO(TeacherOutBodyDTO nt, Map<Integer, TeacherOutBodyDTO> body) {
}
