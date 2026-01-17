package ru.services.logic.people;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.controllers.people.ParentsController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.parents.ParentsBodyDTO;
import ru.data.DTO.controller.people.parents.ParentsBodyUserDTO;
import ru.data.DTO.controller.people.parents.ParentsInnerDTO;
import ru.data.DTO.controller.people.parents.ParentsOutDTO;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.security.user.Roles;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.people.IParentsService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RU: сервис для контроллера
 * @see ParentsController*/
@Service
@RequiredArgsConstructor
public class ParentsService implements IParentsService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final IDBService dbService;
    private final RoleRepository roleRepository;
    private final IUserService userService;

    @Override
    public ParentsOutDTO deleteRoleUser(User user1, Group group) {
        final var dTOBuilder = ParentsOutDTO.builder();

        user1.getRoles().remove(Roles.PARENT);
        userRepository.saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1);
        groupRepository.saveAndFlush(group);

        dTOBuilder.id(user1.getId());
        return dTOBuilder.build();
    }

    @Override
    public ParentsOutDTO changeFIO(User user1, String name) {
        final var dTOBuilder = ParentsOutDTO.builder();

        user1.setFio(name);
        userRepository.saveAndFlush(user1);

        dTOBuilder.id(user1.getId())
            .name(name);
        return dTOBuilder.build();
    }

    @Override
    public ParentsOutDTO addNewAccountWithRole(User kidU, ParentsInnerDTO body, SubscriberDTO sub) {
        final var dTOBuilder = ParentsOutDTO.builder();
        final var bodyDTOBuilder = ParentsBodyDTO.builder();
        final Map<String, ParentsBodyDTO> parentsInnerDTO = body.bod.par();
        final Map<String, ParentsBodyDTO> parentsOutDTO = new HashMap<>();
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final School school = dbService.schoolById(Long.parseLong(sub.getLvlSch()));

        dTOBuilder.id(kidU.getId());
        bodyDTOBuilder.name(kidU.getFio())
            .login(kidU.getUsername());
        for (ParentsBodyDTO parent : parentsInnerDTO.values()) {
            createNewParent(kidU, parent, school, dateAfter, parentsOutDTO);
        }
        bodyDTOBuilder.par(parentsOutDTO);
        dTOBuilder.body(bodyDTOBuilder.build());
        return dTOBuilder.build();
    }

    private void createNewParent(User kidU, ParentsBodyDTO parent, School school, Date dateAfter, Map<String, ParentsBodyDTO> parentsOutDTO) {
        final Role role = roleRepository.saveAndFlush(new Role(null, school));
        final User inv = new User(parent.name(), Map.of(
            Roles.PARENT, role
        ), AppConfig.dataFormat.format(dateAfter));
        userRepository.saveAndFlush(inv);

        parentsOutDTO.put(inv.getId() + "", new ParentsBodyDTO(inv.getFio()));

        final List<User> kids = inv.getRole(Roles.PARENT).getKids();
        if (!kids.contains(kidU)) {
            kids.add(kidU);
        }
        kidU.getRole(Roles.KID).getParents().add(inv);
        userRepository.saveAndFlush(kidU);
    }

    @Override
    public ParentsOutDTO prepareParents(Group group) {
        final var dTOBuilder = ParentsOutDTO.builder();
        final Map<Long, ParentsBodyUserDTO> parentsDTO = new HashMap<>();

        if (!ObjectUtils.isEmpty(group.getKids())) {
            for (User studU : group.getKids()) {
                final ParentsBodyUserDTO.ParentsBodyUserDTOBuilder bodyDTOBuilder = ParentsBodyUserDTO.builder();
                if (studU == null) continue;

                bodyDTOBuilder.name(studU.getFio());
                if (!ObjectUtils.isEmpty(studU.getUsername())) {
                    bodyDTOBuilder.login(studU.getUsername());
                }
                if (!ObjectUtils.isEmpty(studU.getCode())) {
                    bodyDTOBuilder.link(studU.getCode());
                }
                final List<User> parents = studU.getRole(Roles.KID).getParents();
                bodyDTOBuilder.par(userService.usersByListEntity(parents, true));
                parentsDTO.put(studU.getId(), bodyDTOBuilder.build());
            }
        }
        dTOBuilder.bodyP(parentsDTO)
            .bodyC(userService.usersByListEntity(group.getKids(), true));
        return dTOBuilder.build();
    }
}
