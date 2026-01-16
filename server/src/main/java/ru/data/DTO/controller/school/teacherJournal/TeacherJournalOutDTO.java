package ru.data.DTO.controller.school.teacherJournal;

import lombok.Builder;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyDayWeekDTO;

import java.util.Map;

@Builder
public record TeacherJournalOutDTO(String homework, String day, Long kid, TeacherJournalBodyMarkDTO body,
   Map<String, String> bodyD, Map<Long, TeacherJournalBodyDTO> bodyK, String min, String max,
   Map<Integer, String> bodyPred, Map<Long, String> bodyPers, Map<Integer, DnevnikBodyDayWeekDTO> bodyS) {
}
