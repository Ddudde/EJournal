package ru.services.logic.main;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.configs.AppConfig;
import ru.controllers.main.SettingsController;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.controller.main.SettingOutDTO;
import ru.data.DTO.controller.main.SettingsInnerDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.services.interfaces.IEmailService;
import ru.services.interfaces.IPushService;
import ru.services.interfaces.logic.main.ISettingsService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** RU: сервис для контроллера
 * @see SettingsController */
@Service
@RequiredArgsConstructor
public class SettingsService implements ISettingsService {
    private final SettingUserRepository settingUserRepository;
    private final IEmailService emailService;
    private final IPushService pushService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void checkCodeVerificationEmail(SettingUser settingUser, SettingsInnerDTO body) {
        settingUser.setEmail(body.email);
        settingUser.setEmailCode(null);
        settingUser.setExpDateEC(null);
        settingUserRepository.saveAndFlush(settingUser);
    }

    @Override
    public void startEmail(SettingUser settingUser, String email) {
        final String code = UUID.randomUUID().toString();
        emailService.sendRegCode(email, code);
        settingUser.setEmailCode(code);
        final Instant after = Instant.now().plus(Duration.ofDays(1));
        final Date dateAfter = Date.from(after);
        settingUser.setExpDateEC(AppConfig.dataFormat.format(dateAfter));
        settingUserRepository.saveAndFlush(settingUser);
    }

    @Override
    public void removeNotifToken(SettingUser settingUser, String notifToken) {
        pushService.remToken(settingUser, notifToken);
        settingUserRepository.saveAndFlush(settingUser);
    }

    @Override
    public void addNotifToken(SettingUser settingUser, String notifToken) {
        pushService.addToken(settingUser, notifToken);
        settingUserRepository.saveAndFlush(settingUser);
    }

    @Override
    public void changeSettings(SettingUser settingUser, SettingsInnerDTO body) {
        switch (body.id) {
            case "chSecFR" -> settingUser.setSecFr(body.valString);
            case "chIco" -> settingUser.setIco(body.valInt);
            case "checkbox_hints" -> settingUser.setHints(body.val);
            case "checkbox_notify" -> setNotificationForUser(body.val, settingUser);
            case "checkbox_notify_sched" -> settingUser.setNChangeShedule(body.val);
            case "checkbox_notify_marks" -> settingUser.setNNewMarks(body.val);
            case "checkbox_notify_yo" -> changeSubscribe("News", body.val, settingUser);
            case "checkbox_notify_por" -> changeSubscribe("news", body.val, settingUser);
            case "checkbox_notify_new_sch" -> settingUser.setNNewReqSch(body.val);
            default -> {}
        }
        settingUserRepository.saveAndFlush(settingUser);
    }

    private void setNotificationForUser(Boolean notif, SettingUser settings) {
        final Set<String> topics = settings.getTopics();
        final Set<String> tokens = settings.getTokens();

        topics.forEach((topic) -> {
            if(notif
            && ((topic.contains("News") && settings.getNNewNewsYO())
            || (topic.contains("news") && settings.getNNewNewsPor()))) {
                pushService.subscribe(new ArrayList<>(tokens), topic);
            } else {
                pushService.unsubscribe(new ArrayList<>(tokens), topic);
            }
        });
        settings.setNotif(notif);
    }

    private void changeSubscribe(String name, boolean enabledSubscribe, SettingUser settings) {
        final Set<String> topics = settings.getTopics();
        final Set<String> tokens = settings.getTokens();

        topics.forEach((topic) -> {
            if(enabledSubscribe && settings.getNotif() && topic.contains(name)) {
                pushService.subscribe(new ArrayList<>(tokens), topic);
            } else { // сброс подписки при переходе между Учебным центром и Порталом
                pushService.unsubscribe(new ArrayList<>(tokens), topic);
            }
        });
        if(Objects.equals(name, "News")) {
            settings.setNNewNewsYO(enabledSubscribe);
        } else {
            settings.setNNewNewsPor(enabledSubscribe);
        }

    }

    @Override
    public void checkPasswordCodeVerificationEmail(User user, SettingUser settingUser, String nPar) {
        settingUser.setEmailCode(null);
        settingUser.setExpDateEC(null);
        settingUserRepository.saveAndFlush(settingUser);
        user.setPassword(passwordEncoder.encode(nPar));
        userRepository.saveAndFlush(user);
    }

    @Override
    public SettingOutDTO changePassword(SettingUser settingUser, SettingsInnerDTO body, User user, boolean empLogin) {
        final String reason = empLogin ? "Изменение пароля в EJournal" : "Восстановление пароля в EJournal";
        final SettingOutDTO.SettingOutDTOBuilder outDTO = SettingOutDTO.builder();
        HttpStatus stat = HttpStatus.ACCEPTED;
        
        if(!body.emailSt && !Objects.equals(settingUser.getSecFr(), body.secFR)){
            outDTO.error("secFr");
        } else if(body.emailSt && !Objects.equals(settingUser.getEmail(), body.email)){
            outDTO.error("email");
        } else {
            if(body.emailSt) {
                final String code = UUID.randomUUID().toString();
                emailService.sendRecCode(body.email, code, reason);
                settingUser.setEmailCode(code);
                final Instant after = Instant.now().plus(Duration.ofDays(1));
                final Date dateAfter = Date.from(after);
                settingUser.setExpDateEC(AppConfig.dataFormat.format(dateAfter));
                settingUserRepository.saveAndFlush(settingUser);
            } else {
                user.setPassword(passwordEncoder.encode(body.nPar));
                userRepository.saveAndFlush(user);
            }
            stat = HttpStatus.OK;
        }
        outDTO.status(stat);
        return outDTO.build();
    }

    @Override
    public SettingOutDTO getSettings(SettingUser settingUser) {
        final SettingOutDTO.SettingOutDTOBuilder outDTO = SettingOutDTO.builder();

        outDTO.checkbox_hints(settingUser.getHints())
            .checkbox_notify(settingUser.getNotif())
            .checkbox_notify_sched(settingUser.getNChangeShedule())
            .checkbox_notify_marks(settingUser.getNNewMarks())
            .checkbox_notify_yo(settingUser.getNNewNewsYO())
            .checkbox_notify_por(settingUser.getNNewNewsPor())
            .checkbox_notify_new_sch(settingUser.getNNewReqSch());
        return outDTO.build();
    }
}
