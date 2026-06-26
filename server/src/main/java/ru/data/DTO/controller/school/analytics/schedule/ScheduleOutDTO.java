package ru.data.DTO.controller.school.analytics.schedule;

import lombok.Builder;
import ru.data.DTO.service.school.TeacherServiceDTO;

import java.util.Map;

@Builder
public record ScheduleOutDTO(Object body, TeacherServiceDTO bodyT, Integer day, Integer les, Long firstG,
     Map<Long, String> bodyG) {
}
