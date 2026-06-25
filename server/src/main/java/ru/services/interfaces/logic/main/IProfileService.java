package ru.services.interfaces.logic.main;

import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.main.profile.ProfileOutDTO;
import ru.data.DTO.service.data.UserServiceDTO;
import ru.security.user.AuthToken;

public interface IProfileService {
    ProfileOutDTO prepareChKidDTO(UserServiceDTO userServiceDTO);

    ProfileOutDTO prepareChRoleDTO(UserServiceDTO userServiceDTO);

    String exitFromAccount(User user, SubscriberDTO sub, String notifToken, AuthToken auth);

    ProfileOutDTO getProfileOfUser(SettingUser settingUser, User user);
}
