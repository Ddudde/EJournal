package ru.services.logic.school;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.school.TeacherJournalController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.*;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.teacherJournal.*;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.data.DTO.service.school.ScheduleServiceDTO;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.services.interfaces.data.IGroupService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.school.ITeacherJournalService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;
import ru.services.interfaces.logic.school.analytics.IScheduleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** RU: сервис для контроллера
 * @see TeacherJournalController */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherJournalService implements ITeacherJournalService {
    private final DayRepository dayRepository;
    private final MarkRepository markRepository;
    private final IDBService dbService;
    private final LessonRepository lessonRepository;
    private final IGroupService groupService;
    private final IScheduleService scheduleService;
    private final IPeriodService periodService;

    @Override
    public TeacherJournalOutDTO addHomework(TeacherJournalInnerDTO body, SubscriberDTO sub, User user, Group group, School school) {
        final String[] lesDay = body.day.split(",");
        Day day = dayRepository
            .findBySchoolIdAndTeacherIdAndGrpIdAndNameSubjectAndDat(school.getId(), user.getId(), group.getId(), sub.getLvlMore2(), lesDay[0]);
        final TeacherJournalOutDTO.TeacherJournalOutDTOBuilder dtoBuilder = TeacherJournalOutDTO.builder();

        if (day == null) {
            day = new Day();
            day.setDat(lesDay[0]);
            day.setGrp(group);
            day.setTeacher(user);
            day.setSchool(school);
            day.setNameSubject(sub.getLvlMore2());
        }
        day.setHomework(body.homework);
        dayRepository.saveAndFlush(day);
        dtoBuilder.day(body.day)
            .homework(body.homework);
        return dtoBuilder.build();
    }

    @Override
    public TeacherJournalOutDTO addMark(TeacherJournalInnerDTO body, SubscriberDTO sub, School school, User user, Group group, User objU) {
        final boolean isNotPeriodMark = body.per == null;
        final PrepareMarkDTO prepareMarkDTO = prepareMarkForCreate(school, user, body, group, sub.getLvlMore2());
        Mark mark = prepareMarkDTO.mark;
        final TeacherJournalOutDTO.TeacherJournalOutDTOBuilder dtoBuilder = TeacherJournalOutDTO.builder();
        final TeacherJournalBodyMarkDTO.TeacherJournalBodyMarkDTOBuilder markDTOBuilder = TeacherJournalBodyMarkDTO.builder();

        if(prepareMarkDTO.mark == null) mark = new Mark();
        mark.setMark(body.mark);
        mark.setUsr(objU);
        mark.setPeriod(prepareMarkDTO.period);
        mark.setType(isNotPeriodMark ? "norm" : "per");
        mark.setWeight(body.weight);
        mark.setStyle(isNotPeriodMark ? body.style : sub.getLvlMore2());
        markRepository.saveAndFlush(mark);
        if(isNotPeriodMark && !prepareMarkDTO.oldMark) {
            prepareMarkDTO.day.getMarks().add(mark);
            dayRepository.saveAndFlush(prepareMarkDTO.day);
        }
        dtoBuilder.kid(objU.getId())
            .day(body.day);
        markDTOBuilder.mark(mark.getMark())
            .weight(mark.getWeight())
            .type(mark.getStyle())
            .per(body.per);
        dtoBuilder.body(markDTOBuilder.build());
        return dtoBuilder.build();
    }

    /**
     * @return PrepareMarkDTO.oldMark.true - оценка существует */
    private PrepareMarkDTO prepareMarkForCreate(School school, User user, TeacherJournalInnerDTO body, Group group, String nameSubject) {
        final boolean isNotPeriodMark = body.per == null;
        PrepareMarkDTO prepareMarkDTO;
        if(!isNotPeriodMark) {
            prepareMarkDTO = new PrepareMarkDTO();
            prepareMarkDTO.period = dbService.periodById(body.per);
            prepareMarkDTO.mark = markRepository
                .findByTypeAndStyleAndPeriodIdAndUsrId("per", nameSubject, body.per, body.kid);
            prepareMarkDTO.oldMark = false;
            return prepareMarkDTO;
        }
        //Видимо отправляется номер последней существующей оценки
        final String[] dayAndNumOfMark = body.day.split(",");
        prepareMarkDTO = getExistMark(body.kid, school, user, group, nameSubject, dayAndNumOfMark);
        if (prepareMarkDTO.day != null) return prepareMarkDTO;

        final Day day = new Day();
        day.setDat(dayAndNumOfMark[0]);
        day.setGrp(group);
        day.setTeacher(user);
        day.setSchool(school);
        day.setNameSubject(nameSubject);
        dayRepository.saveAndFlush(day);
        prepareMarkDTO.day = day;
        return prepareMarkDTO;
    }

    //toDo: перепроверить #numLes
    private PrepareMarkDTO getExistMark(Long idKid, School school, User user, Group group, String nameSubject, String[] dayAndNumOfMark) {
        final PrepareMarkDTO prepareMarkDTO = new PrepareMarkDTO();
        prepareMarkDTO.period = periodService.getActualPeriodBySchool(school);
        final Map<String, List<Long>> marksIdByDay = getMarksByDay(school.getId(), user.getId(), group.getId(), nameSubject);
        if (ObjectUtils.isEmpty(marksIdByDay)) return prepareMarkDTO;

        final List<Day> days = dayRepository
            .findBySchoolIdAndTeacherIdAndGrpIdAndNameSubject(school.getId(), user.getId(), group.getId(), nameSubject);
        int numLes = 0;
        if (dayAndNumOfMark.length > 1) {
            numLes = Integer.parseInt(dayAndNumOfMark[1]) + 1;
        }
        if (!marksIdByDay.containsKey(dayAndNumOfMark[0])) return prepareMarkDTO;

        final List<Mark> marksOfKidAndDay = markRepository
            .findByIdInAndUsrId(marksIdByDay.get(dayAndNumOfMark[0]), idKid);
        log.trace(marksOfKidAndDay + "");
        if (!ObjectUtils.isEmpty(marksOfKidAndDay)) {
            prepareMarkDTO.mark = marksOfKidAndDay.get(numLes);
            prepareMarkDTO.day = days.get(numLes);
            prepareMarkDTO.oldMark = true;
        }
        return prepareMarkDTO;
    }

    /** RU: получает оценки всей группы в рамках дисциплины по дням */
    private Map<String, List<Long>> getMarksByDay(Long schoolId, Long userId, Long groupId, String nameSubject) {
        final List<Object[]> marksByDay = dayRepository
            .uniqDatAndMarksByParams(schoolId, userId, groupId, nameSubject);
        if (ObjectUtils.isEmpty(marksByDay)) return null;

        return marksByDay.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collector.of(
                ArrayList<Long>::new,
                (list, item) -> list.add((Long) item[1]),
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
            )));
    }

    @Override
    public TeacherJournalOutDTO prepareMarksAndHomeworksForGroup(SubscriberDTO sub, School school, User user, Group group) {
        final TeacherJournalOutDTO.TeacherJournalOutDTOBuilder dtoBuilder = TeacherJournalOutDTO.builder();
        final TeacherJournalBodyMarkDTO.TeacherJournalBodyMarkDTOBuilder markDTOBuilder = TeacherJournalBodyMarkDTO.builder();

        final Map<String, List<Long>> marksByDay = getMarksByDay(school.getId(), user.getId(), group.getId(), sub.getLvlMore2());
        log.trace(marksByDay + "");
        final List<Object[]> homeworks = dayRepository
            .uniqDatAndHomeworkByParams(school.getId(), group.getId(), sub.getLvlMore2());
        final Map<String, String> homeworkByDay = homeworks.stream()
            .filter(obj->obj[0] != null && obj[1] != null)
            .collect(Collectors.toMap(s -> (String) s[0], s -> (String) s[1],
                (first, second) -> first));
        if (!ObjectUtils.isEmpty(homeworkByDay)) {
            dtoBuilder.bodyD(homeworkByDay);
        }

        dtoBuilder.bodyK(getJournalAndPeriods(marksByDay, group.getKids(), school, sub.getLvlMore2()));
        return dtoBuilder.build();
    }

    /** RU: заполняет JSON.
     * Оценки группы данного периода и итоговые на дисциплине.
     * <pre>
     * bodyK : {
     *     idKid : {
     *         name : "FIO",
     *         days : {
     *             stringDate : {
     *                 "stringMark",
     *                 weight,
     *                 "type"
     *             }
     *         },
     *         avg : {
     *             idPeriod : "stringMark"
     *         }
     *     }
     * }
     * </pre>
     * @param marksByDay Map с оценками. По дате */
    private Map<Long, TeacherJournalBodyDTO> getJournalAndPeriods(Map<String, List<Long>> marksByDay, List<User> kids, School school, String nameSubject) {
        final Period actPeriod = periodService.getActualPeriodBySchool(school);
        final Map<Long, TeacherJournalBodyDTO> marksByKidIdDTO = new HashMap<>();
        if (ObjectUtils.isEmpty(kids) || ObjectUtils.isEmpty(marksByDay)) {
            return marksByKidIdDTO;
        }

        for (User kid : kids) {
            if (kid == null) continue;

            TeacherJournalBodyDTO journalBodyDTO = prepareMarksAndMarkPeriodsForKid(marksByDay, school, nameSubject, kid, actPeriod);
            marksByKidIdDTO.put(kid.getId(), journalBodyDTO);
        }
        return marksByKidIdDTO;
    }

    private TeacherJournalBodyDTO prepareMarksAndMarkPeriodsForKid(Map<String, List<Long>> marksByDay, School school, String nameSubject, User kid, Period actPeriod) {
        final TeacherJournalBodyDTO.TeacherJournalBodyDTOBuilder dtoBuilder = TeacherJournalBodyDTO.builder();
        final Map<String, TeacherJournalBodyMarkDTO> marksByDateDTO = new HashMap<>();
        final Map<Long, String> marksByPeriodDTO = new HashMap<>();

        dtoBuilder.name(kid.getFio());
        for (String dat : marksByDay.keySet()) {
            //Уточняет по отдельному ученику и периоду обучения из оценок всей группы
            final List<Mark> marksOfKid = markRepository
                .findByIdInAndUsrIdAndPeriodId(marksByDay.get(dat), kid.getId(), actPeriod.getId());
            log.trace(dat);
            log.trace(marksOfKid + "");

            int i1 = -1;
            for (Mark marksM : marksOfKid) {
                final TeacherJournalBodyMarkDTO.TeacherJournalBodyMarkDTOBuilder markDTOBuilder = TeacherJournalBodyMarkDTO.builder();
                String dateAndNum = dat;
                if(i1 != -1) dateAndNum = dat + "," + i1;

                markDTOBuilder.mark(marksM.getMark())
                    .weight(marksM.getWeight())
                    .type(marksM.getStyle());
                marksByDateDTO.put(dateAndNum, markDTOBuilder.build());
                i1++;
            }
        }
        dtoBuilder.days(marksByDateDTO);

        final List<Mark> periodMarksOfKid = markRepository
            .findByPeriodInAndTypeAndStyleAndUsrId(school.getPeriods(), "per", nameSubject, kid.getId());
        log.trace("perU " + periodMarksOfKid);
        for (Mark marksM : periodMarksOfKid) {
            marksByPeriodDTO.put(marksM.getPeriod().getId(), marksM.getMark());
        }
        dtoBuilder.avg(marksByPeriodDTO);
        return dtoBuilder.build();
    }

    @Override
    public GroupServiceDTO groupsByList(Long schoolId, String nameSubject, Long userId) {
        final List<Long> groupsL = lessonRepository
            .uniqGroupsBySchoolAndSubNameAndTeacher(schoolId, nameSubject, userId);
        if (ObjectUtils.isEmpty(groupsL)) return null;

        log.trace(groupsL + "");
        return groupService.groupsByList(groupsL);
    }

    @Override
    public TeacherJournalOutDTO prepareScheduleAndPeriods(School school, User user) {
        final List<String> subjs = lessonRepository
            .uniqSubNameBySchoolAndTeacher(school.getId(), user.getId());
        final List<Period> periods = school.getPeriods();
        final Period actPeriod = periodService.getActualPeriodBySchool(school);
        final TeacherJournalOutDTO.TeacherJournalOutDTOBuilder dtoBuilder = TeacherJournalOutDTO.builder();
        final Map<Integer, String> subjectNameForTeacherDTO = new HashMap<>();
        final Map<Long, String> namePeriodsByIdDTO = new HashMap<>();

        if (!ObjectUtils.isEmpty(subjs)){
            log.trace(String.valueOf(subjs));
            int i = 0;

            for (String name : subjs) {
                subjectNameForTeacherDTO.put(i, name);
                i++;
            }
            dtoBuilder.bodyPred(subjectNameForTeacherDTO);
        }
        if (!ObjectUtils.isEmpty(periods)){
            for (Period p : periods) {
                namePeriodsByIdDTO.put(p.getId(), p.getName());
            }
            dtoBuilder.bodyPers(namePeriodsByIdDTO);
        }
        dtoBuilder.min(actPeriod.getDateN())
            .max(actPeriod.getDateK());
        final ScheduleServiceDTO shedule = scheduleService.getShedule(user, null);
        dtoBuilder.bodyS(shedule.body());
        return dtoBuilder.build();
    }
}
