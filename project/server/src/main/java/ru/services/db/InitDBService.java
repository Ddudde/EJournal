package ru.services.db;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.Roles;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** RU: Класс для рандомизация данных, тестовые данные для БД */
@Slf4j
@RequiredArgsConstructor
@Service public class InitDBService {
    private final PasswordEncoder passwordEncoder;
    private final SettingUserRepository settingUserRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final IDBService dbService;
    private final IRandomizeService randomService;

    @PostConstruct
    public void postConstruct() {
//        log.info("test234");
        final SettingUser setts = settingUserRepository.saveAndFlush(new SettingUser(1));
        final Role role = roleRepository.saveAndFlush(new Role("ex@ya.ru"));
        final User user = userRepository
            .saveAndFlush(new User("nm12", passwordEncoder.encode("1111"),
            "Петров В.В.", Map.of(
            Roles.ADMIN, role
        ), Roles.ADMIN, setts));
        if(AppConfig.TEST) randomService.createRandomData();
        checkDates();
    }

    /** RU: удаление инвайта для новой роли */
    private void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            userRepository.saveAndFlush(user);
        }
    }

    /** RU: удаление инвайта для регистрации нового пользователя */
    private void delInv(User inv) {
        if(inv != null) {
            final School school = dbService.getFirstRole(inv.getRoles()).getYO();
            school.getHteachers().remove(inv);
            schoolRepository.saveAndFlush(school);
            userRepository.delete(inv);
        }
    }

    /** RU: проверяет коды подтверждений электронных почт и инвайтов на истечение срока */
    private void checkDates() {
        try {
            final long now = AppConfig.dataFormat.parse(AppConfig.dataFormat.format(new Date())).getTime();
            final List<User> listUsers = userRepository.findAll();
            for(User user : listUsers) {
                String expectDate = user.getExpDate();
                if(!ObjectUtils.isEmpty(expectDate) && now >= AppConfig.dataFormat.parse(expectDate).getTime()){
                    log.debug("Удалён код " + user.getCode() + " по истечению срока действия");
                    if(user.getUsername() == null) {
                        delInv(user);
                    } else {
                        delCodeUser(user);
                    }
                }
                final SettingUser settingUser = user.getSettings();
                if(settingUser != null) {
                    expectDate = settingUser.getExpDateEC();
                    if (!ObjectUtils.isEmpty(expectDate) && now >= AppConfig.dataFormat.parse(expectDate).getTime()) {
                        log.debug("Удалён код email" + settingUser.getEmailCode() + " по истечению срока действия");
                        settingUser.setEmailCode(null);
                        settingUser.setExpDateEC(null);
                        settingUserRepository.saveAndFlush(settingUser);
                    }
                }
            }
        } catch (ParseException e) {
            log.debug(e.getMessage());
        }
    }
}
