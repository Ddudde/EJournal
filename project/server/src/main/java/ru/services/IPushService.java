package ru.services;

import ru.data.DAO.auth.SettingUser;

import java.util.List;

public interface IPushService {
    void send(String topic, String title, String text, String imgUrl);

    void send(List<String> registrationTokens, String title, String text, String imgUrl);

    int subscribe(List<String> registrationTokens, String topic);

    int unsubscribe(List<String> registrationTokens, String topic);

    void addToken(SettingUser settingUser, String token);

    void remToken(SettingUser settingUser, String token);

    void addTopic(SettingUser settingUser, String topic);

    void remTopic(SettingUser settingUser, String topic);
}
