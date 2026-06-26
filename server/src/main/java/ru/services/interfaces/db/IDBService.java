package ru.services.interfaces.db;

import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.Request;
import ru.data.DAO.school.School;
import ru.security.user.AuthToken;
import ru.security.user.Roles;

import java.util.List;
import java.util.Map;

public interface IDBService {
    SettingUser createSettingUser(SettingUser settingUser);

    User userByLogin(String login);

    User userByCode(String code);

    boolean existUserById(Long id);

    boolean existUserByAuth(AuthToken authToken);

    User userById(Long id);

    Roles getFirstRoleId(Map<Roles, Role> map);

    Role getFirstRole(Map<Roles, Role> map);

    List<Request> getRequests();

    Request requestById(Long id);

    List<School> getSchools();

    School schoolById(Long id);

    Syst createSyst(Syst syst);

    Syst getSyst();

    News newsById(Long id);

    Group groupById(Long id);

    Period periodById(Long id);
}
