package ru.data.DTO.controller.school.analytics.kidJournal;

import lombok.Builder;

@Builder
public record KidJournalMarksDTO(String mark, Integer weight, String type) {
}
