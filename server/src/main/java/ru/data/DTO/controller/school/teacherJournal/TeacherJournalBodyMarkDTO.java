package ru.data.DTO.controller.school.teacherJournal;

import lombok.Builder;

@Builder
public record TeacherJournalBodyMarkDTO(String mark, String type, Integer weight, Long per) {
}
