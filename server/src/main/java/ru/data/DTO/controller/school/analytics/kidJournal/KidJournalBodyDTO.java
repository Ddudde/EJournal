package ru.data.DTO.controller.school.analytics.kidJournal;

import java.util.Map;

public record KidJournalBodyDTO(Map<String, KidJournalMarksDTO> days) {
}
