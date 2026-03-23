package ru.controllers.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.main.SettingOutDTO;
import ru.data.DTO.controller.main.SettingsInnerDTO;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.main.ISettingsService;

import java.util.Objects;

/** RU: Контроллер для раздела настроек
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/SettingsController">http://localhost:9001/swagger/htmlSwag/#/SettingsController</a>
 * </pre> */
@Slf4j
@RequestMapping("/settings")
@RequiredArgsConstructor
@RestController public class SettingsController {
    private final IDBService dbService;
    private final ISettingsService settingsService;

    /** RU: подтверждение емэйла
     * @see DocsHelpController#point Описание */
    @PatchMapping("/checkCodeEmail")
    public ResponseEntity<Void> checkCodeEmail(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
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
        
        settingsService.checkCodeVerificationEmail(settingUser, body);
        return ResponseEntity.ok().build();
    }

    /** RU: изменение электронной почты пользователя или добавление при регистрации
     * @see DocsHelpController#point Описание */
    @PatchMapping("/startEmail")
    public ResponseEntity<Void> startEmail(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        User user = null;
        if(!ObjectUtils.isEmpty(body.invCod)) {
            user = dbService.userByCode(body.invCod);
        } else {
            user = dbService.userById(sub.getUserId());
        }
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if(ObjectUtils.isEmpty(body.email)) return ResponseEntity.notFound().build();

        settingsService.startEmail(user.getSettings(), body.email);
        return ResponseEntity.ok().build();
    }

    /** RU: удаление токена уведомлений
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/remNotifToken")
    public ResponseEntity<Void> remNotifToken(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        if(ObjectUtils.isEmpty(body.notifToken)) return ResponseEntity.notFound().build();

        settingsService.removeNotifToken(user.getSettings(), body.notifToken);
        return ResponseEntity.ok().build();
    }

    /** RU: установка токена уведомлений
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/addNotifToken")
    public ResponseEntity<Void> addNotifToken(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        if(ObjectUtils.isEmpty(body.notifToken)) return ResponseEntity.notFound().build();

        settingsService.addNotifToken(user.getSettings(), body.notifToken);
        return ResponseEntity.ok().build();
    }

    /** RU: Вкл/выкл подсказки или ряд уведомлений
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chSettings")
    public ResponseEntity<Void> chSettings(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        if(ObjectUtils.isEmpty(body.id)) return ResponseEntity.notFound().build();

        settingsService.changeSettings(user.getSettings(), body);
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/checkPasCodeEmail")
    public ResponseEntity<Void> checkPasCodeEmail(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final boolean empLogin = ObjectUtils.isEmpty(body.login);
        final User user = empLogin ? dbService.userById(sub.getUserId()) : dbService.userByLogin(body.login);
        if(!Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
            return ResponseEntity.notFound().build();
        }

        settingsService.checkPasswordCodeVerificationEmail(user, user.getSettings(), body.nPar);
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла/секретной фразы
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chPass")
    public ResponseEntity<SettingOutDTO> chPass(@RequestBody SettingsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final boolean empLogin = ObjectUtils.isEmpty(body.login);
        final User user = empLogin ? dbService.userById(sub.getUserId()) : dbService.userByLogin(body.login);

        final SettingOutDTO settingOutDTO = settingsService.changePassword(user.getSettings(), body, user, empLogin);
        return ResponseEntity.status(settingOutDTO.status)
            .body(settingOutDTO);
    }

    /** RU: отправляет настройки клиенту
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getSettings")
    public ResponseEntity<SettingOutDTO> getSettings(@AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final SettingUser settingUser = user.getSettings();
        if(settingUser == null) return ResponseEntity.notFound().build();

        final SettingOutDTO settingOutDTO = settingsService.getSettings(settingUser);
        return ResponseEntity.ok().body(settingOutDTO);
    }
}