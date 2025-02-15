package ru.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.services.EmailService;
import ru.services.MainService;
import ru.services.PushService;
import ru.services.db.DBService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/** RU: Контроллер для раздела настроек
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/SettingsController">http://localhost:9001/swagger/htmlSwag/#/SettingsController</a>
 * </pre> */
@Slf4j
@RequestMapping("/settings")
@RequiredArgsConstructor
@RestController public class SettingsController {
    private final PasswordEncoder passwordEncoder;
    private final MainService mainService;
    private final SettingUserRepository settingUserRepository;
    private final EmailService emailService;
    private final PushService pushService;
    private final UserRepository userRepository;
    private final DBService dbService;

    /** RU: подтверждение емэйла
     * @see DocsHelpController#point(Object, Object) Описание */
    @PatchMapping("/checkCodeEmail")
    public ResponseEntity<Void> checkCodeEmail(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        log.info("[PATCH] /checkCodeEmail ! " + body);
        User user = null;
        if(!ObjectUtils.isEmpty(body.invCod)) {
            user = dbService.userByCode(body.invCod);
        } else {
            user = dbService.userById(sub.getUserId());
        }
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        final SettingUser settingUser = user.getSettings();
        if(!Objects.equals(settingUser.getEmailCode(), body.emailCode)) {
            return ResponseEntity.notFound().build();
        }
        settingUser.setEmail(body.email);
        settingUser.setEmailCode(null);
        settingUser.setExpDateEC(null);
        settingUserRepository.saveAndFlush(settingUser);
        return ResponseEntity.ok().build();
    }

    /** RU: изменение электронной почты пользователя или добавление при регистрации
     * @see DocsHelpController#point(Object, Object) Описание */
    @PatchMapping("/startEmail")
    public ResponseEntity<Void> startEmail(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        log.info("[PATCH] /startEmail ! " + body);
        User user = null;
        if(!ObjectUtils.isEmpty(body.invCod)) {
            user = dbService.userByCode(body.invCod);
        } else {
            user = dbService.userById(sub.getUserId());
        }
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if(ObjectUtils.isEmpty(body.email)) return ResponseEntity.notFound().build();

        final SettingUser settingUser = user.getSettings();
        final String code = UUID.randomUUID().toString();
        emailService.sendRegCode(body.email, code);
        settingUser.setEmailCode(code);
        final Instant after = Instant.now().plus(Duration.ofDays(1));
        final Date dateAfter = Date.from(after);
        settingUser.setExpDateEC(MainService.df.format(dateAfter));
        settingUserRepository.saveAndFlush(settingUser);
        return ResponseEntity.ok().build();
    }

    /** RU: удаление токена уведомлений
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/remNotifToken")
    public ResponseEntity<Void> remNotifToken(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        log.info("[POST] /remNotifToken ! " + body);
        if(ObjectUtils.isEmpty(body.notifToken)) return ResponseEntity.notFound().build();

        final SettingUser settingUser = user.getSettings();
        pushService.remToken(settingUser, body.notifToken);
        settingUserRepository.saveAndFlush(settingUser);
        return ResponseEntity.ok().build();
    }

    /** RU: установка токена уведомлений
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/addNotifToken")
    public ResponseEntity<Void> addNotifToken(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        log.info("[POST] /addNotifToken ! " + body);
        if(ObjectUtils.isEmpty(body.notifToken)) return ResponseEntity.notFound().build();

        final SettingUser settingUser = user.getSettings();
        pushService.addToken(settingUser, body.notifToken);
        settingUserRepository.saveAndFlush(settingUser);
        return ResponseEntity.ok().build();
    }

    /** RU: Вкл/выкл подсказки или ряд уведомлений
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chSettings")
    public ResponseEntity<Void> chSettings(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        log.info("[PATCH] /chBool ! " + body);
        if(ObjectUtils.isEmpty(body.id)) return ResponseEntity.notFound().build();

        final SettingUser settingUser = user.getSettings();
        switch (body.id) {
            case "chSecFR" -> settingUser.setSecFr(body.valString);
            case "chIco" -> settingUser.setIco(body.valInt);
            case "checkbox_hints" -> settingUser.setHints(body.val);
            case "checkbox_notify" -> settingUser.setNotif(body.val);
            case "checkbox_notify_sched" -> settingUser.setNChangeShedule(body.val);
            case "checkbox_notify_marks" -> settingUser.setNNewMarks(body.val);
            case "checkbox_notify_yo" -> settingUser.setNNewNewsYO(body.val);
            case "checkbox_notify_por" -> settingUser.setNNewNewsPor(body.val);
            case "checkbox_notify_new_sch" -> settingUser.setNNewReqSch(body.val);
            default -> {}
        }
        settingUserRepository.saveAndFlush(settingUser);
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/checkPasCodeEmail")
    public ResponseEntity<Void> checkPasCodeEmail(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final boolean empLogin = ObjectUtils.isEmpty(body.login);
        log.info("[PATCH] /checkPasCodeEmail ! " + body);
        final User user = empLogin ? dbService.userById(sub.getUserId()) : dbService.userByLogin(body.login);
        if(!Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
            return ResponseEntity.notFound().build();
        }
        user.getSettings().setEmailCode(null);
        user.getSettings().setExpDateEC(null);
        settingUserRepository.saveAndFlush(user.getSettings());
        user.setPassword(passwordEncoder.encode(body.nPar));
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла/секретной фразы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chPass")
    public ResponseEntity<JsonObject> chPass(@RequestBody DataSettings body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final boolean empLogin = ObjectUtils.isEmpty(body.login);
        final User user = empLogin ? dbService.userById(sub.getUserId()) : dbService.userByLogin(body.login);
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chPass");
        HttpStatus stat = HttpStatus.ACCEPTED;
        final SettingUser settingUser = user.getSettings();
        if(!body.emailSt && !Objects.equals(settingUser.getSecFr(), body.secFR)){
            wrtr.name("error").value("secFr");
        } else if(body.emailSt && !Objects.equals(settingUser.getEmail(), body.email)){
            wrtr.name("error").value("email");
        } else {
            if(body.emailSt) {
                final String code = UUID.randomUUID().toString();
                emailService.sendRecCode(body.email, code, empLogin ? "Изменение пароля в EJournal" : "Восстановление пароля в EJournal");
                settingUser.setEmailCode(code);
                final Instant after = Instant.now().plus(Duration.ofDays(1));
                final Date dateAfter = Date.from(after);
                settingUser.setExpDateEC(MainService.df.format(dateAfter));
                settingUserRepository.saveAndFlush(settingUser);
            } else {
                user.setPassword(passwordEncoder.encode(body.nPar));
                userRepository.saveAndFlush(user);
            }
            stat = HttpStatus.OK;
        }
        return mainService.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: отправляет настройки клиенту
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getSettings")
    public ResponseEntity<JsonObject> getSettings(@AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getSettings");
        final SettingUser settingUser = user.getSettings();
        if(settingUser == null) return ResponseEntity.notFound().build();

        wrtr.name("checkbox_hints").value(settingUser.getHints())
            .name("checkbox_notify").value(settingUser.getNotif())
            .name("checkbox_notify_sched").value(settingUser.getNChangeShedule())
            .name("checkbox_notify_marks").value(settingUser.getNNewMarks())
            .name("checkbox_notify_yo").value(settingUser.getNNewNewsYO())
            .name("checkbox_notify_por").value(settingUser.getNNewNewsPor())
            .name("checkbox_notify_new_sch").value(settingUser.getNNewReqSch());
        return mainService.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые SettingsController в методах
     * @see SettingsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataSettings {
        public final String secFR, nPar, id, email, emailCode, login,
            notifToken, invCod, valString;
        public final int valInt;
        public final boolean val, emailSt;
    }
}