package ru.services.logic.people;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.controllers.people.StudentsController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.people.students.StudentsOutBodyDTO;
import ru.data.DTO.controller.people.students.StudentsOutDTO;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.security.user.Roles;
import ru.services.db.IDBService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/** RU: сервис для контроллера
 * @see StudentsController */
@Service
@RequiredArgsConstructor
public class StudentsService implements IStudentsService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final IDBService dbService;

    @Override
    public StudentsOutDTO deleteRoleUser(User user1, Group group) {
        user1.getRoles().remove(Roles.HTEACHER);
        userRepository.saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) {
            group.getKids().remove(user1);
        }
        groupRepository.saveAndFlush(group);

        return new StudentsOutDTO(user1.getId());
    }

    @Override
    public StudentsOutDTO changeFIO(User user1, String name) {
        final StudentsOutDTO.StudentsOutDTOBuilder dtoBuilder = StudentsOutDTO.builder();

        user1.setFio(name);
        userRepository.saveAndFlush(user1);

        dtoBuilder.id(user1.getId())
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public StudentsOutDTO addNewAccountWithRole(Long schoolId, Group group, String name) {
        final StudentsOutDTO.StudentsOutDTOBuilder dtoBuilder = StudentsOutDTO.builder();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final School school = dbService.schoolById(schoolId);
        final Role role = roleRepository.saveAndFlush(new Role(null, school, group));
        final User inv = new User(name, Map.of(
                Roles.KID, role
        ), AppConfig.dataFormat.format(dateAfter));
        userRepository.saveAndFlush(inv);
        group.getKids().add(inv);
        groupRepository.saveAndFlush(group);

        dtoBuilder.id(inv.getId())
                .body(new StudentsOutBodyDTO(name));
        return dtoBuilder.build();
    }
}
