package ru.services.logic.people;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.configs.AppConfig;
import ru.controllers.people.HTeachersController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.people.hTeacher.HTeachersBodyDTO;
import ru.data.DTO.controller.people.hTeacher.HTeachersOutDTO;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.Roles;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.people.IHTeachersService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RU: сервис для контроллера
 * @see HTeachersController */
@Service
@RequiredArgsConstructor
public class HTeachersService implements IHTeachersService {
    private final SchoolRepository schoolRepository;
    private final GroupRepository groupRepository;
    private final IDBService dbService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IUserService userService;

    @Override
    public HTeachersOutDTO removeGroup(Long grId, School school) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        final Group group = dbService.groupById(grId);
        groupRepository.delete(group);
        school.getGroups().remove(group);
        schoolRepository.saveAndFlush(school);
        dtoBuilder.id(group.getId());
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO addGroup(String name, School school) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        final Group group = new Group(name);
        groupRepository.saveAndFlush(group);
        school.getGroups().add(group);
        schoolRepository.saveAndFlush(school);
        dtoBuilder.id(group.getId())
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO changeNameGroup(String name, Group group) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        group.setName(name);
        groupRepository.saveAndFlush(group);
        dtoBuilder.id(group.getId())
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO changeFIO(User user1, String name, Long schoolId) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        user1.setFio(name);
        userRepository.saveAndFlush(user1);
        dtoBuilder.id(user1.getId())
            .id1(schoolId)
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO deleteRoleUser(User user1, School school) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        user1.getRoles().remove(Roles.HTEACHER);
        userRepository.saveAndFlush(user1);
        school.getHteachers().remove(user1);
        schoolRepository.saveAndFlush(school);
        dtoBuilder.id(user1.getId())
            .id1(school.getId());
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO addNewAccountWithRole(String name, School sch) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = roleRepository.saveAndFlush(new Role(null, sch));
        final User inv = new User(name, Map.of(
                Roles.HTEACHER, role
        ), AppConfig.dataFormat.format(dateAfter));

        userRepository.saveAndFlush(inv);
        sch.getHteachers().add(inv);
        schoolRepository.saveAndFlush(sch);

        dtoBuilder.id1(sch.getId())
            .id(inv.getId())
            .body(new HTeachersBodyDTO(name));
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO changeNameSchool(String name, School school) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        school.setName(name);
        schoolRepository.saveAndFlush(school);

        dtoBuilder.id(school.getId())
            .name(name);
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO addSchool(String name) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();
        final School school = new School(name);

        schoolRepository.saveAndFlush(school);
        dtoBuilder.id(school.getId())
            .body(new HTeachersBodyDTO(name));
        return dtoBuilder.build();
    }

    @Override
    public HTeachersOutDTO deleteSchool(School school, Long schId) {
        final HTeachersOutDTO.HTeachersOutDTOBuilder dtoBuilder = HTeachersOutDTO.builder();

        schoolRepository.delete(school);

        dtoBuilder.id(schId);
        return dtoBuilder.build();
    }

    @Override
    public Map<Long, HTeachersBodyDTO> prepareInfoForAdmins(List<School> schools) {
        final Map<Long, HTeachersBodyDTO> map = new HashMap<>();

        for (School el : schools) {
            final HTeachersBodyDTO.HTeachersBodyDTOBuilder bodyDTOBuilder = HTeachersBodyDTO.builder();

            bodyDTOBuilder.name(el.getName())
                .pep(userService.usersByListEntity(el.getHteachers(), true));
            map.put(el.getId(), bodyDTOBuilder.build());
        }
        return map;
    }
}
