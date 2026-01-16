package ru.services.logic.main;

import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.main.profile.ProfileOutDTO;
import ru.data.DTO.service.data.UserServiceDTO;

public interface IProfileService {
    ProfileOutDTO prepareChKidDTO(UserServiceDTO userServiceDTO);

    ProfileOutDTO prepareChRoleDTO(UserServiceDTO userServiceDTO);

    void exitFromAccount(User user, SubscriberDTO sub, String notifToken);

    ProfileOutDTO getProfileOfUser(SettingUser settingUser, User user);
}
