package ru.data.DTO.controller.school.teacherJournal;

import lombok.Builder;

import java.util.Map;

@Builder
public record TeacherJournalBodyDTO(String name, Map<String, TeacherJournalBodyMarkDTO> days, Map<Long, String> avg) {
}
