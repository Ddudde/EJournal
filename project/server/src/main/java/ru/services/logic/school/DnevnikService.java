package ru.services.logic.school;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.school.DnevnikController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Mark;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyDateDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyMarkDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikOutDTO;
import ru.data.DTO.service.school.ScheduleServiceDTO;
import ru.data.reps.school.DayRepository;
import ru.services.db.IDBService;
import ru.services.logic.school.analytics.IPeriodService;
import ru.services.logic.school.analytics.IScheduleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** RU: сервис для контроллера
 * @see DnevnikController */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnevnikService implements IDnevnikService {
    private final DayRepository dayRepository;
    private final IDBService dbService;
    private final IScheduleService scheduleService;
    private final IPeriodService periodService;

    @Override
    public DnevnikOutDTO prepareDnevnik(User user, Long groupId) {
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        final ScheduleServiceDTO serviceDTO = scheduleService.getShedule(user, groupId);

        final Period actPeriod = periodService.getActualPeriodBySchool(school);
        final List<Object[]> marks = dayRepository
            .uniqNameSubjectAndDatAndMarksByParams(school.getId(), groupId, actPeriod.getId());
        final Map<String, Map<String, List<Mark>>> mapD = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.groupingBy(
                obj1 -> (String) obj1[1],
                Collector.of(
                    ArrayList<Mark>::new,
                    (list, item) -> list.add((Mark) item[2]),
                    (left, right) -> right
                ))));
        log.trace("mapD " + mapD);
        final List<Object[]> homeworks = dayRepository
            .uniqNameSubAndDatAndHomeworkByParams(school.getId(), groupId);
        final Map<String, Map<String, String>> mapH = homeworks.stream()
            .filter(obj->obj[0] != null && obj[1] != null && obj[2] != null)
            .collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.toMap(obj -> (String) obj[1], obj -> (String) obj[2],
                    (first, second) -> first)
            ));

        final Map<String, Map<String, DnevnikBodyDateDTO>> nameSubjectsByDates = getJournal(mapD, mapH);
        return new DnevnikOutDTO(serviceDTO.body(), actPeriod.getDateN(), actPeriod.getDateK(), nameSubjectsByDates);
    }

    /**
     * RU: Заполняет структуру для JSON.
     * Если оценки существуют, то заполняет (оценки + домашние задания), в противном случае просто домашние задания.
     * <pre>
     * bodyD : {
     *     "nameSubject" : {
     *         "dateOfEvent" : {
     *             marks : {
     *                 intNumMark : {
     *                     "stringMark",
     *                     weight,
     *                     "type",
     *                     "homework"
     *                 }
     *             },
     *             i: 0
     *         }
     *     }
     * }
     * </pre>
     *
     * toDo: исправить структуру на клиенте, DnevnikBodyDateDTO добавлен уровень marks
     * @param marksByDateAndNameSubject Map с оценками. По названию дисциплины и дате
     * @param homeworksByDateAndNameSubject Map с домашними заданиями. По названию дисциплины и дате
     */
    private Map<String, Map<String, DnevnikBodyDateDTO>> getJournal(Map<String, Map<String, List<Mark>>> marksByDateAndNameSubject, Map<String, Map<String, String>> homeworksByDateAndNameSubject) {
        if(ObjectUtils.isEmpty(marksByDateAndNameSubject)) {
            return prepareHomework(homeworksByDateAndNameSubject);
        }
        final boolean isEmptyHomeworks = ObjectUtils.isEmpty(homeworksByDateAndNameSubject);
        final Map<String, Map<String, DnevnikBodyDateDTO>> nameSubjectsByDates = new HashMap<>();

        for (String nameSub : marksByDateAndNameSubject.keySet()) {
            final Map<String, List<Mark>> marksByDates = marksByDateAndNameSubject.get(nameSub);
            final Map<String, DnevnikBodyDateDTO> datesByMarks = new HashMap<>();
            Map<String, String> homeworksByDate = null;
            if(!isEmptyHomeworks && homeworksByDateAndNameSubject.containsKey(nameSub)) {
                homeworksByDate = homeworksByDateAndNameSubject.get(nameSub);
            }
            if (ObjectUtils.isEmpty(marksByDates)) continue;

            for (String dat : marksByDates.keySet()) {
                final Map<Integer, DnevnikBodyMarkDTO> marksByNum = new HashMap<>();
                int i1 = 0;

                for (Mark marksM : marksByDates.get(dat)) {
                    final DnevnikBodyMarkDTO.DnevnikBodyMarkDTOBuilder dtoBuilder = DnevnikBodyMarkDTO.builder();

                    dtoBuilder.mark(marksM.getMark())
                        .weight(marksM.getWeight())
                        .type(marksM.getStyle());
                    if (i1 == 0 && !ObjectUtils.isEmpty(homeworksByDate) && homeworksByDate.containsKey(dat)) {
                        dtoBuilder.homework(homeworksByDate.get(dat));
                    }
                    marksByNum.put(i1++, dtoBuilder.build());
                }
                datesByMarks.put(dat, new DnevnikBodyDateDTO(marksByNum, 0));
            }
            nameSubjectsByDates.put(nameSub, datesByMarks);
        }
        return nameSubjectsByDates;
    }

    private Map<String, Map<String, DnevnikBodyDateDTO>> prepareHomework(Map<String, Map<String, String>> homeworksByDateAndNameSubject) {
        final Map<String, Map<String, DnevnikBodyDateDTO>> nameSubjectsByDates = new HashMap<>();
        if(ObjectUtils.isEmpty(homeworksByDateAndNameSubject)) return null;

        for (String nameSub : homeworksByDateAndNameSubject.keySet()) {
            final Map<String, DnevnikBodyDateDTO> datesByMarks = new HashMap<>();
            if (!homeworksByDateAndNameSubject.containsKey(nameSub)) continue;
            final Map<String, String> homeworksByDate = homeworksByDateAndNameSubject.get(nameSub);

            for (String dat : homeworksByDate.keySet()) {
                final Map<Integer, DnevnikBodyMarkDTO> marksByNum = new HashMap<>();
                final DnevnikBodyMarkDTO.DnevnikBodyMarkDTOBuilder dtoBuilder = DnevnikBodyMarkDTO.builder();

                if (homeworksByDate.containsKey(dat)) {
                    dtoBuilder.homework(homeworksByDate.get(dat));
                }
                marksByNum.put(0, dtoBuilder.build());
                datesByMarks.put(dat, new DnevnikBodyDateDTO(marksByNum, 0));
            }
            nameSubjectsByDates.put(nameSub, datesByMarks);
        }
        return nameSubjectsByDates;
    }
}
