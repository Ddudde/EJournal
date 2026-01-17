package ru.services.logic.school.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.school.analytics.KidJournalController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Mark;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.school.analytics.kidJournal.KidJournalBodyDTO;
import ru.data.DTO.controller.school.analytics.kidJournal.KidJournalMarksDTO;
import ru.data.DTO.controller.school.analytics.kidJournal.KidJournalOutDTO;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.services.interfaces.logic.school.analytics.IKidJournalService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** RU: сервис для контроллера
 * @see KidJournalController */
@Slf4j
@Service
@RequiredArgsConstructor
public class KidJournalService implements IKidJournalService {
    private final MarkRepository markRepository;
    private final LessonRepository lessonRepository;
    private final DayRepository dayRepository;
    private final IPeriodService periodService;

    @Override
    public KidJournalOutDTO preparePeriodMarks(List<Period> periods, User user, School sch, Group group) {
        final KidJournalOutDTO.KidJournalOutDTOBuilder dtoBuilder = KidJournalOutDTO.builder();
        final Map<Long, String> nameByIdPeriodsDTO = new HashMap<>();
        final Map<String, Map<Long, String>> marksByIdAndNameSubjectsDTO = new HashMap<>();
        for (Period p : periods) {
            nameByIdPeriodsDTO.put(p.getId(), p.getName());
        }
        dtoBuilder.bodyPers(nameByIdPeriodsDTO);
        final List<Object[]> marks = markRepository
            .uniqNameSubjectAndMarksByParams(user.getId(), "per", periods);
        final Map<String, List<Mark>> marksByNameSubjects = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collector.of(
                ArrayList<Mark>::new,
                (list, item) -> list.add((Mark) item[1]),
                (left, right) -> right
            )));
        log.trace(marksByNameSubjects + "");
        if(ObjectUtils.isEmpty(marksByNameSubjects)) return dtoBuilder.build();

        final List<String> lessonsByKid = lessonRepository
            .uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
        for(String les : lessonsByKid) {
            if(marksByNameSubjects.containsKey(les)) continue;
            marksByNameSubjects.put(les, null);
        }
        for (Map.Entry<String, List<Mark>> entry : marksByNameSubjects.entrySet()) {
            final String nameSubject = entry.getKey();
            final Map<Long, String> marksByIdDTO = new HashMap<>();

            for (Mark marksM : entry.getValue()) {
                marksByIdDTO.put(marksM.getPeriod().getId(), marksM.getMark());
            }
            marksByIdAndNameSubjectsDTO.put(nameSubject, marksByIdDTO);
        }
        dtoBuilder.bodyM(marksByIdAndNameSubjectsDTO);
        return dtoBuilder.build();
    }

    @Override
    public KidJournalOutDTO prepareInfo(School sch, Group group) {
        final Period actPeriod = periodService.getActualPeriodBySchool(sch);
        final List<Object[]> marks = dayRepository
            .uniqNameSubjectAndDatAndMarksByParams(sch.getId(), group.getId(), actPeriod.getId());
        final Map<String, Map<String, List<Mark>>> marksByDateAndNameSubject = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.groupingBy(
                obj1 -> (String) obj1[1],
                Collector.of(
                    ArrayList<Mark>::new,
                    (list, item) -> list.add((Mark) item[2]),
                    (left, right) -> right
                ))));
        log.trace("mapD " + marksByDateAndNameSubject);
        if(ObjectUtils.isEmpty(marksByDateAndNameSubject)) return null;

        final List<String> lessonsByKid = lessonRepository
            .uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
        for(String les : lessonsByKid) {
            if(marksByDateAndNameSubject.containsKey(les)) continue;
            marksByDateAndNameSubject.put(les, null);
        }
        return getJournal(marksByDateAndNameSubject);
    }

    /** RU: Заполняет JSON.
     * Заполняет оценки с учётом возможности нескольких оценок за день.
     * <pre>
     * bodyJ : {
     *     "nameSubject" : {
     *         "days" : {
     *             "dateOfEvent" + intNumMark: {
     *                 "stringMark",
     *                 weight,
     *                 "type"
     *             }
     *         }
     *     }
     * }
     * </pre>
     * @param marksByDateAndNameSubject Map с оценками. По названию дисциплины и дате */
    private KidJournalOutDTO getJournal(Map<String, Map<String, List<Mark>>> marksByDateAndNameSubject) {
        final KidJournalOutDTO.KidJournalOutDTOBuilder dtoBuilder = KidJournalOutDTO.builder();
        final Map<String, KidJournalBodyDTO> daysByNameSubjectDTO = new HashMap<>();

        for (String nameSub : marksByDateAndNameSubject.keySet()) {
            final Map<String, List<Mark>> marksByDate = marksByDateAndNameSubject.get(nameSub);
            final Map<String, KidJournalMarksDTO> marksByDateDTO = new HashMap<>();
            if(ObjectUtils.isEmpty(marksByDate)) continue;

            for (Map.Entry<String, List<Mark>> entry : marksByDate.entrySet()) {
                int i1 = -1;
                final String dat = entry.getKey();

                for (Mark marksM : entry.getValue()) {
                    final KidJournalMarksDTO.KidJournalMarksDTOBuilder marksDTOBuilder = KidJournalMarksDTO.builder();
                    String dateAndNum = dat;
                    if(i1 != -1) dateAndNum = dat + "," + i1;

                    marksDTOBuilder.mark(marksM.getMark())
                        .weight(marksM.getWeight())
                        .type(marksM.getStyle());
                    marksByDateDTO.put(dateAndNum, marksDTOBuilder.build());
                    i1++;
                }
            }
            daysByNameSubjectDTO.put(nameSub, new KidJournalBodyDTO(marksByDateDTO));
        }
        dtoBuilder.bodyJ(daysByNameSubjectDTO);
        return dtoBuilder.build();
    }
}
