package ru.services.logic.people;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.controllers.people.TeachersController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.teacher.TeacherOutBodyDTO;
import ru.data.DTO.controller.people.teacher.TeacherOutDTO;
import ru.data.DTO.service.school.TeacherServiceDTO;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.data.IUserService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** RU: сервис для контроллера
 * @see TeachersController */
@Service
@RequiredArgsConstructor
public class TeacherService implements ITeacherService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final LessonRepository lessonRepository;
    private final IUserService userService;

    @Override
    public TeacherOutDTO deleteRoleUser(User user1, Group group) {
        user1.getRoles().remove(Roles.TEACHER);
        userRepository.saveAndFlush(user1);
//        if (!ObjectUtils.isEmpty(group.getKids())) {
//            group.getKids().remove(user1);
//        }
//        groupRepository.saveAndFlush(group);

        return new TeacherOutDTO(user1.getId());
    }

    @Override
    public TeacherOutDTO changeFIO(User user1, String name) {
        final TeacherOutDTO.TeacherOutDTOBuilder dtoBuilder = TeacherOutDTO.builder();

        user1.setFio(name);
        userRepository.saveAndFlush(user1);

        dtoBuilder.id(user1.getId())
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public TeacherOutDTO addNewAccountWithRole(School school, String name) {
        final TeacherOutDTO.TeacherOutDTOBuilder dtoBuilder = TeacherOutDTO.builder();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = roleRepository.saveAndFlush(new Role(null, Set.of(), school));
        final User inv = new User(name, Map.of(
                Roles.TEACHER, role
        ), AppConfig.dataFormat.format(dateAfter));
        userRepository.saveAndFlush(inv);
        school.getTeachers().add(inv);
        schoolRepository.saveAndFlush(school);

        dtoBuilder.id(inv.getId())
            .name(name);
        return dtoBuilder.build();
    }

    /** RU: готовит JSON с данными списка учителей.
     * <pre>
     * nt : {
     *     tea : {{@link #usersByList}}
     * },
     * body : {
     *    intNumSubject : {
     *        "name",
     *        tea : {{@link #usersByList}}
     *    }
     * }
     * nt - учителя принадлежащие школе, но ещё не прикреплённые к дисциплинам.
     * body - определённая дисциплина и учителя, которые её преподают
     * </pre>
     * toDo: подправить в клиенте изменение, появление body
     * @see TeachersController#getTeachers(CustomToken, SubscriberDTO)   Пример использования */
    @SuppressWarnings("JavadocReference")
    @Override
    public TeacherServiceDTO teachersBySchool(School school) {
        final List<Object[]> rawData = lessonRepository.uniqTeachersLBySchool(school.getId());
        final TeacherServiceDTO.TeacherServiceDTOBuilder dtoBuilder = TeacherServiceDTO.builder();
        final Map<Integer, TeacherOutBodyDTO> body = new HashMap<>();

        final var tea = userService.usersByListEntity(school.getTeachers(), true);
        dtoBuilder.nt(new TeacherOutBodyDTO(tea));
        if (ObjectUtils.isEmpty(rawData)) return dtoBuilder.build();

        final Map<String, List<Long>> nameSubjectOfListTeachers = rawData.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
        ));
        int i = 0;
        for (Map.Entry<String, List<Long>> entryOfTeachers : nameSubjectOfListTeachers.entrySet()) {
            final TeacherOutBodyDTO.TeacherOutBodyDTOBuilder bodyDTOBuilder = TeacherOutBodyDTO.builder();

            bodyDTOBuilder.name(entryOfTeachers.getKey())
                .tea(userService.usersByListId(entryOfTeachers.getValue(), true));
            body.put(i, bodyDTOBuilder.build());
            i++;
        }
        dtoBuilder.body(body);
        return dtoBuilder.build();
    }
}
