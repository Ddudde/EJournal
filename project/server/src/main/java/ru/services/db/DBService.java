package ru.services.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.Request;
import ru.data.DAO.school.School;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
import ru.security.user.Roles;

import java.util.List;
import java.util.Map;

/** RU: сервис с различными геттерами к БД */
@Slf4j
@Getter
@AllArgsConstructor
@Service public class DBService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RequestRepository requestRepository;
    private final SystRepository systRepository;
    private final NewsRepository newsRepository;
    private final ContactsRepository contactsRepository;
    private final GroupRepository groupRepository;
    private final DayRepository dayRepository;
    private final LessonRepository lessonRepository;
    private final MarkRepository markRepository;
    private final SettingUserRepository settingUserRepository;
    private final PeriodRepository periodRepository;
    private final RoleRepository roleRepository;

    public SettingUser createSettingUser(SettingUser settingUser) {
        SettingUser savedSettingUser = settingUserRepository.saveAndFlush(settingUser);
        log.trace(savedSettingUser + "");
        return savedSettingUser;
    }

    public User userByLogin(String login) {
        if(login == null) return null;
        return userRepository.findByUsername(login);
    }

    public User userByCode(String code) {
        return userRepository.findByCode(code);
    }

    public User userById(Long id) {
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    public Roles getFirstRoleId(Map<Roles, Role> map) {
        return map.keySet().iterator().next();
    }

    public Role getFirstRole(Map<Roles, Role> map) {
        return map.get(getFirstRoleId(map));
    }

    public List<Request> getRequests() {
        return requestRepository.findAll();
    }

    public Request requestById(Long id) {
        return id == null ? null : requestRepository.findById(id).orElse(null);
    }

    public List<School> getSchools() {
        return schoolRepository.findAll();
    }

    public School schoolById(Long id) {
        return id == null ? null : schoolRepository.findById(id).orElse(null);
    }

    public Syst createSyst(Syst syst) {
        Syst savedSyst = systRepository.saveAndFlush(syst);
        log.trace(savedSyst + "");
        return savedSyst;
    }

    public Syst getSyst() {
        List<Syst> systs = systRepository.findAll();
        return systs.isEmpty() ? null : systs.get(0);
    }

    public News newsById(Long id) {
        return id == null ? null : newsRepository.findById(id).orElse(null);
    }

    public Group groupById(Long id) {
        return id == null ? null : groupRepository.findById(id).orElse(null);
    }

    public Period periodById(Long id) {
        return id == null ? null : periodRepository.findById(id).orElse(null);
    }
}
