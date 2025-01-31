package ru.services.db;

import lombok.AllArgsConstructor;
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
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.data.reps.school.PeriodRepository;
import ru.data.reps.school.RequestRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.Roles;

import java.util.List;
import java.util.Map;

/** RU: сервис с различными геттерами к БД */
@Slf4j
@AllArgsConstructor
@Service(value = "dbService")
public class DBService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RequestRepository requestRepository;
    private final SystRepository systRepository;
    private final NewsRepository newsRepository;
    private final GroupRepository groupRepository;
    private final SettingUserRepository settingUserRepository;
    private final PeriodRepository periodRepository;

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

    public boolean existUserById(Long id) {
        if(id == null) return false;

        return userRepository.existsById(id);
    }

    public boolean existUserBySubscription(SubscriberDTO subscriberDTO) {
        return existUserById(subscriberDTO.getUserId());
    }

    public User userById(Long id) {
        if(id == null) return null;

        return userRepository.findById(id).orElse(null);
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
        if(id == null) return null;

        return requestRepository.findById(id).orElse(null);
    }

    public List<School> getSchools() {
        return schoolRepository.findAll();
    }

    public School schoolById(Long id) {
        if(id == null) return null;

        return schoolRepository.findById(id).orElse(null);
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
        if(id == null) return null;

        return newsRepository.findById(id).orElse(null);
    }

    public Group groupById(Long id) {
        if(id == null) return null;

        return groupRepository.findById(id).orElse(null);
    }

    public Period periodById(Long id) {
        if(id == null) return null;

        return periodRepository.findById(id).orElse(null);
    }
}
