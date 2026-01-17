package ru.services.logic.people;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.configs.AppConfig;
import ru.controllers.people.AdminsController;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DTO.controller.people.admin.AdminsOutBodyDTO;
import ru.data.DTO.controller.people.admin.AdminsOutDTO;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.user.Roles;
import ru.services.interfaces.logic.people.IAdminsService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/** RU: сервис для контроллера
 * @see AdminsController*/
@Service
@RequiredArgsConstructor
public class AdminsService implements IAdminsService {
    private final UserRepository userRepository;
    private final SystRepository systRepository;
    private final RoleRepository roleRepository;

    @Override
    public AdminsOutDTO deleteRoleUser(User user1, Syst syst) {
        final AdminsOutDTO.AdminsOutDTOBuilder builderDTO = AdminsOutDTO.builder();

        user1.getRoles().remove(Roles.ADMIN);
        userRepository.saveAndFlush(user1);
        syst.getAdmins().remove(user1);
        systRepository.saveAndFlush(syst);

        builderDTO.id(user1.getId());
        return builderDTO.build();
    }

    @Override
    public AdminsOutDTO changeFIO(User user1, String name) {
        final AdminsOutDTO.AdminsOutDTOBuilder builderDTO = AdminsOutDTO.builder();

        user1.setFio(name);
        userRepository.saveAndFlush(user1);

        builderDTO.id(user1.getId())
            .name(name);
        return builderDTO.build();
    }

    @Override
    public AdminsOutDTO addNewAccountWithRole(Syst syst, String name) {
        final AdminsOutDTO.AdminsOutDTOBuilder builderDTO = AdminsOutDTO.builder();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = roleRepository.saveAndFlush(new Role());
        final User inv = new User(name, Map.of(
            Roles.ADMIN, role
        ), AppConfig.dataFormat.format(dateAfter));
        userRepository.saveAndFlush(inv);
        syst.getAdmins().add(inv);
        systRepository.saveAndFlush(syst);

        builderDTO.id(inv.getId())
            .body(new AdminsOutBodyDTO(name));
        return builderDTO.build();
    }
}
