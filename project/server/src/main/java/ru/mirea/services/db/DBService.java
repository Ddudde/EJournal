package ru.mirea.services.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Period;
import ru.mirea.data.models.school.Request;
import ru.mirea.data.models.school.School;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.NewsRepository;
import ru.mirea.data.reps.SystRepository;
import ru.mirea.data.reps.auth.RoleRepository;
import ru.mirea.data.reps.auth.SettingUserRepository;
import ru.mirea.data.reps.auth.UserRepository;
import ru.mirea.data.reps.school.*;
import ru.mirea.data.reps.school.DayRepository;
import ru.mirea.data.reps.school.MarkRepository;

import java.util.List;
import java.util.Map;

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

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public SettingUser createSettingUser(SettingUser settingUser) {
        SettingUser savedSettingUser = settingUserRepository.saveAndFlush(settingUser);
        System.out.println(savedSettingUser);
        return savedSettingUser;
    }

    public User userByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User userByCode(String code) {
        return userRepository.findByCode(code);
    }

    public User userById(Long id) {
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    public Long getFirstRoleId(Map<Long, Role> map) {
        return (Long) map.keySet().toArray()[0];
    }

    public Role getFirstRole(Map<Long, Role> map) {
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
        System.out.println(savedSyst);
        return savedSyst;
    }

    public Syst getSyst() {
        List<Syst> systs = systRepository.findAll();
        return systs.isEmpty() ? null : systs.get(0);
    }

    public News newsById(Long id) {
        return id == null ? null : newsRepository.findById(id).orElse(null);
    }

    public Contacts contactsById(Long id) {
        return id == null ? null : contactsRepository.findById(id).orElse(null);
    }

    public Group groupById(Long id) {
        return id == null ? null : groupRepository.findById(id).orElse(null);
    }

    public Period periodById(Long id) {
        return id == null ? null : periodRepository.findById(id).orElse(null);
    }
}
