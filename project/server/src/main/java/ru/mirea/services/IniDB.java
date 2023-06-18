package ru.mirea.services;

import ru.mirea.Main;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;

import java.util.Map;

public class IniDB {

    private final ServerService serv;

    public IniDB(ServerService serverService) {
        serv = serverService;
        SettingUser setts = serv.getSettingUserRepository().saveAndFlush(new SettingUser(1));
        Role role = serv.getRoleRepository().saveAndFlush(new Role("ex@ya.ru"));
        User user = serv.getUserRepository().saveAndFlush(new User("nm12", "1111",
            "Петров В.В.", Map.of(
            4L, role
        ), 4L, 1L, setts));
        if(Main.test) serv.testOn();
    }
}
