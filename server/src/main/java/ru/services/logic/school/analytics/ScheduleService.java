package ru.services.logic.school.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.school.analytics.ScheduleController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Lesson;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleInnerBodyDTO;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleInnerDTO;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleOutDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyDayWeekDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikBodyLessonDTO;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.data.DTO.service.school.ScheduleServiceDTO;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.interfaces.data.IGroupService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.people.ITeacherService;
import ru.services.interfaces.logic.school.analytics.IScheduleService;

import java.util.*;

/** RU: сервис для контроллера
 * @see ScheduleController */
@Service
@RequiredArgsConstructor
public class ScheduleService implements IScheduleService {
    private final SchoolRepository schoolRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ITeacherService teacherService;
    private final IDBService dbService;
    private final IGroupService groupService;

    @Override
    public ScheduleOutDTO addLesson(ScheduleInnerDTO body, User teaU, School school, Group group) {
        final ScheduleOutDTO.ScheduleOutDTOBuilder dtoBuilder = ScheduleOutDTO.builder();
        final ScheduleInnerBodyDTO bodyDTO = body.obj();

        final Lesson lesson = new Lesson();
        applyToTeacher(teaU, lesson, school.getTeachers());
        lesson.setNameSubject(bodyDTO.name);
        lesson.setKab(bodyDTO.cabinet);
        lesson.setGrp(group);
        lesson.setSchool(school);
        lesson.setDayWeek(body.day());
        final List<Lesson> lessons = lessonRepository.findBySchoolIdAndGrpIdAndDayWeek(school.getId(), group.getId(), body.day());
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        if(lessons.isEmpty()) {
            lesson.setNumLesson(0);
        } else {
            lesson.setNumLesson(lessons.getLast().getNumLesson()+1);
        }
        lessonRepository.saveAndFlush(lesson);
        schoolRepository.saveAndFlush(school);
        dtoBuilder.bodyT(teacherService.teachersBySchool(school));
        bodyDTO.group = group.getName();
        dtoBuilder.body(bodyDTO)
            .day(body.day())
            .les(lesson.getNumLesson());
        return dtoBuilder.build();
    }

    private void applyToTeacher(User teaU, Lesson lesson, List<User> teachers) {
        if(teaU == null) return;
        final Set<String> subjects = teaU.getRole(Roles.TEACHER).getSubjects();

        lesson.setTeacher(teaU);
        if(!ObjectUtils.isEmpty(teachers)) {
            //удаление из списка учителей не вписанных в расписание
            teachers.remove(teaU);
        }
        if(!subjects.contains(lesson.getNameSubject())) {
            subjects.add(lesson.getNameSubject());
            userRepository.saveAndFlush(teaU);
        }
    }

    /**
     * RU: готовит JSON с данными расписания для педагога или группы
     * <pre>
     * body : {
     *     intDayWeek : {
     *         "lessons" : {
     *             intNumLesson : {
     *                 "name",
     *                 "cabinet",
     *                 "group",
     *                 "prepod" : {
     *                     "id",
     *                     "name"
     *                 }
     *             }
     *         }
     *     }
     * }
     * </pre>
     *
     * @see ScheduleController#getSchedule(Long, SubscriberDTO, CustomToken)  Пример использования
     */
    @Override
    public ScheduleServiceDTO getShedule(User user, Long groupId) {
        final Long schId = dbService.getFirstRole(user.getRoles()).getYO().getId();
        final Map<Integer, DnevnikBodyDayWeekDTO> daysWeek = new HashMap<>();
        Map<Integer, DnevnikBodyLessonDTO> lessons = new HashMap<>();
        List<Lesson> rawLessons;
        if (user.getSelRole() == Roles.TEACHER) {
            rawLessons = lessonRepository.findBySchoolIdAndTeacherId(schId, user.getId());
        } else {
            rawLessons = lessonRepository.findBySchoolIdAndGrpId(schId, groupId);
        }

        rawLessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        int k1 = -1;
        for (Lesson les : rawLessons) {
            final DnevnikBodyLessonDTO.DnevnikBodyLessonDTOBuilder dtoBuilder = DnevnikBodyLessonDTO.builder();

            if (k1 != les.getDayWeek()) {
                k1 = les.getDayWeek();;
                lessons = new HashMap<>();
                daysWeek.put(k1, new DnevnikBodyDayWeekDTO(lessons));
            }
            if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                dtoBuilder.name(les.getNameSubject());
            }
            dtoBuilder.cabinet(les.getKab());
            if (user.getSelRole() == Roles.TEACHER) {
                final Group grp = les.getGrp();
                dtoBuilder.group(grp.getName());
            } else {
                final User teaU = les.getTeacher();
                if (teaU != null) {
                    dtoBuilder.prepod(new DnevnikBodyDTO(teaU.getId(), teaU.getFio()));
                }
            }
            lessons.put(les.getNumLesson(), dtoBuilder.build());
        }
        return new ScheduleServiceDTO(daysWeek);
    }

    @Override
    public ScheduleOutDTO prepareInfoForHTeacherOrTEACHER(User user, School school) {
        final ScheduleOutDTO.ScheduleOutDTOBuilder dtoBuilder = ScheduleOutDTO.builder();

        final GroupServiceDTO groupDTO = groupService.groupsBySchoolOfUser(user);
        dtoBuilder.bodyG(groupDTO.bodyG())
            .firstG(groupDTO.firstG())
            .bodyT(teacherService.teachersBySchool(school));
        return dtoBuilder.build();
    }
}
