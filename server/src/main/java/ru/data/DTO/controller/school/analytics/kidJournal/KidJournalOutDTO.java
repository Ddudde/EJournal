package ru.data.DTO.controller.school.analytics.kidJournal;

import lombok.Builder;

import java.util.Map;

@Builder
public record KidJournalOutDTO(Map<Long, String> bodyPers, Map<String, Map<Long, String>> bodyM,
   Map<String, KidJournalBodyDTO> bodyJ) {
}
