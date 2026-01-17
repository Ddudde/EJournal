package ru.services.interfaces.logic.main;

import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.controller.main.SettingOutDTO;
import ru.data.DTO.controller.main.SettingsInnerDTO;

public interface ISettingsService {
    void checkCodeVerificationEmail(SettingUser settingUser, SettingsInnerDTO body);

    void startEmail(SettingUser settingUser, String email);

    void removeNotifToken(SettingUser settingUser, String notifToken);

    void addNotifToken(SettingUser settingUser, String notifToken);

    void changeSettings(SettingUser settingUser, SettingsInnerDTO body);

    void checkPasswordCodeVerificationEmail(User user, SettingUser settingUser, String nPar);

    SettingOutDTO changePassword(SettingUser settingUser, SettingsInnerDTO body, User user, boolean empLogin);

    SettingOutDTO getSettings(SettingUser settingUser);
}
