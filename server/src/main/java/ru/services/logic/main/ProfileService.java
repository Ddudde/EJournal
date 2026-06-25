package ru.services.logic.main;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.SecurityConfig;
import ru.controllers.main.ProfileController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.main.profile.ProfileOutBodyRolesDTO;
import ru.data.DTO.controller.main.profile.ProfileOutDTO;
import ru.data.DTO.service.data.UserServiceDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.IPushService;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.logic.main.IProfileService;

import java.util.HashMap;
import java.util.Map;

/** RU: Сервис для контроллера
 * @see ProfileController */
@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {
    private final IPushService pushService;
    private final SettingUserRepository settingUserRepository;
    private final IUserService userService;

    /** @see ProfileController#chKid */
    @Override
    public ProfileOutDTO prepareChKidDTO(UserServiceDTO userServiceDTO) {
        return ProfileOutDTO.builder()
            .kid(userServiceDTO.kid()).build();
    }

    /** @see ProfileController#chRole */
    @Override
    public ProfileOutDTO prepareChRoleDTO(UserServiceDTO userServiceDTO) {
        return ProfileOutDTO.builder()
            .kid(userServiceDTO.kid())
            .role(userServiceDTO.role())
            .kids(userServiceDTO.kids()).build();
    }

    /** RU: очищение авторизации */
    @Override
    public String exitFromAccount(User user, SubscriberDTO sub, String notifToken, AuthToken auth) {
        if (!ObjectUtils.isEmpty(notifToken)) {
            final SettingUser settingUser = user.getSettings();
            pushService.remToken(settingUser, notifToken);
            settingUserRepository.saveAndFlush(settingUser);
        }
        auth.setUserId(null);
        sub.setLvlSch(null);
        sub.setLvlGr(null);

        final ResponseCookie deleteCoookie = ResponseCookie.from(SecurityConfig.NAME_OF_COOKIE, null).path("/auth/refreshToken").httpOnly(false)
            .maxAge(0).sameSite("Strict").build();
        return deleteCoookie.toString();
    }

    @Override
    public ProfileOutDTO getProfileOfUser(SettingUser settingUser, User user) {
        final ProfileOutDTO.ProfileOutDTOBuilder builderDTO = ProfileOutDTO.builder();
        builderDTO.login(user.getUsername())
            .ico(settingUser.getIco())
            .id(user.getId());
        if (!ObjectUtils.isEmpty(user.getFio())) {
            builderDTO.fio(user.getFio());
        }
        if (!ObjectUtils.isEmpty(settingUser.getInfo())) {
            builderDTO.more(settingUser.getInfo());
        }
        final Map<Integer, ProfileOutBodyRolesDTO> roles = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            prepareRoleToDTO(user, i, roles);
        }
        builderDTO.roles(roles);
        return builderDTO.build();
    }

    private void prepareRoleToDTO(User user, int i, Map<Integer, ProfileOutBodyRolesDTO> roles) {
        final Roles roleI = Roles.roleByInteger(i);
        final ProfileOutBodyRolesDTO.ProfileOutBodyRolesDTOBuilder rolesDTOBuilder = ProfileOutBodyRolesDTO.builder();
        if (!user.getRoles().containsKey(roleI)) return;

        final Role role = user.getRoles().get(roleI);
        if (!ObjectUtils.isEmpty(role.getEmail())) {
            rolesDTOBuilder.email(role.getEmail());
        }
        if (!ObjectUtils.isEmpty(role.getYO())) {
            final School school = role.getYO();
            if (school != null) {
                rolesDTOBuilder.yo(school.getName());
            }
        }
        if (!ObjectUtils.isEmpty(role.getGrp())) {
            final Group group = role.getGrp();
            if (group != null) {
                rolesDTOBuilder.group(group.getName());
            }
        }
        if (!ObjectUtils.isEmpty(role.getSubjects())) {
            rolesDTOBuilder.lessons(role.getSubjects());
        }
        if (!ObjectUtils.isEmpty(role.getKids())) {
            final Map<Long, UserServiceBodyUserDTO> kids = userService.usersByListEntity(role.getKids(), false);
            rolesDTOBuilder.kids(kids);
        }
        if (!ObjectUtils.isEmpty(role.getParents())) {
            final Map<Long, UserServiceBodyUserDTO> parents = userService.usersByListEntity(role.getParents(), false);
            rolesDTOBuilder.parents(parents);
        }
        roles.put(i, rolesDTOBuilder.build());
    }

}
