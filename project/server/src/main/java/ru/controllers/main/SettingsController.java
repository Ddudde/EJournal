package ru.controllers.main;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.Main;
import ru.controllers.AuthController;
import ru.data.models.auth.SettingUser;
import ru.data.models.auth.User;
import ru.security.user.CustomToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static ru.Main.datas;

/** RU: Контроллер для раздела настроек
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/SettingsController">http://localhost:9001/swagger/htmlSwag/#/SettingsController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre> */
@RequestMapping("/settings")
@RequiredArgsConstructor
@RestController public class SettingsController {

    /** RU: Контроллер авторизации + сервис */
    private final AuthController authController;

    /** RU: подтверждение емэйла
     * @param body Данные с клиента, задействуются свойства: invCod, emailCode, email
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/checkCodeEmail")
    public ResponseEntity<Void> checkCodeEmail(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        System.out.println("[PATCH] /checkCodeEmail ! " + body.toString());
        HttpStatus stat = HttpStatus.NOT_FOUND;
        User user = null;
        if(!ObjectUtils.isEmpty(body.invCod)) {
            user = datas.getDbService().userByCode(body.invCod);
        } else {
            user = auth.getSub().getUser();
        }
        if(user != null && Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
            SettingUser settingUser = user.getSettings();
            settingUser.setEmail(body.email);
            settingUser.setEmailCode(null);
            settingUser.setExpDateEC(null);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: изменение электронной почты пользователя или добавление при регистрации
     * @param body Данные с клиента, задействуются свойства: invCod, email
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/startEmail")
    public ResponseEntity<Void> startEmail(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        System.out.println("[PATCH] /startEmail ! " + body.toString());
        User user = null;
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(!ObjectUtils.isEmpty(body.invCod)) {
            user = datas.getDbService().userByCode(body.invCod);
        } else {
            user = auth.getSub().getUser();
        }
        if(user != null && !ObjectUtils.isEmpty(body.email)) {
            String code = UUID.randomUUID().toString();
            datas.getEmailService().sendRegCode(body.email, code);
            user.getSettings().setEmailCode(code);
            Instant after = Instant.now().plus(Duration.ofDays(1));
            Date dateAfter = Date.from(after);
            user.getSettings().setExpDateEC(Main.df.format(dateAfter));
            datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: удаление токена уведомлений
     * @param body Данные с клиента, задействуются свойства: notifToken
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping("/remNotifToken")
    public ResponseEntity<Void> remNotifToken(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        System.out.println("[POST] /remNotifToken ! " + body.toString());
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            User user = auth.getSub().getUser();
            SettingUser settingUser = user.getSettings();
            datas.getPushService().remToken(settingUser, body.notifToken);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: установка токена уведомлений
     * @param body Данные с клиента, задействуются свойства: notifToken
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping("/addNotifToken")
    public ResponseEntity<Void> addNotifToken(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        System.out.println("[POST] /addNotifToken ! " + body.toString());
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(!ObjectUtils.isEmpty(body.notifToken)) {
            User user = auth.getSub().getUser();
            SettingUser settingUser = user.getSettings();
            datas.getPushService().addToken(settingUser, body.notifToken);
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: Вкл/выкл подсказки или ряд уведомлений
     * @param body Данные с клиента, задействуются свойства: val, id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chSettings")
    public ResponseEntity<Void> chSettings(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        System.out.println("[PATCH] /chBool ! " + body.toString());
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && !ObjectUtils.isEmpty(body.id)) {
            SettingUser settingUser = user.getSettings();
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
            datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла
     * @param body Данные с клиента, задействуются свойства: emailCode, nPar
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/checkPasCodeEmail")
    public ResponseEntity<Void> checkPasCodeEmail(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        boolean empLogin = ObjectUtils.isEmpty(body.login);
        System.out.println("[PATCH] /checkPasCodeEmail ! " + body.toString());
        User user = empLogin ? auth.getSub().getUser() : datas.getDbService().userByLogin(body.login);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && Objects.equals(user.getSettings().getEmailCode(), body.emailCode)) {
            user.getSettings().setEmailCode(null);
            user.getSettings().setExpDateEC(null);
            datas.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
            user.setPassword(body.nPar);
            datas.getDbService().getUserRepository().saveAndFlush(user);
            stat = HttpStatus.OK;
        }
        return ResponseEntity.status(stat).build();
    }

    /** RU: изменяет пароль пользователя при помощи емэйла/секретной фразы
     * @param body Данные с клиента, задействуются свойства: login, emailSt, secFR, email, nPar
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping("/chPass")
    public ResponseEntity<JsonObject> chPass(@RequestBody DataSettings body, CustomToken auth) throws Exception {
        boolean empLogin = ObjectUtils.isEmpty(body.login);
        User user = empLogin ? auth.getSub().getUser() : datas.getDbService().userByLogin(body.login);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chPass");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null) {
            SettingUser settingUser = user.getSettings();
            if(!body.emailSt && !Objects.equals(settingUser.getSecFr(), body.secFR)){
                wrtr.name("error").value("secFr");
                stat = HttpStatus.ACCEPTED;
            } else if(body.emailSt && !Objects.equals(settingUser.getEmail(), body.email)){
                wrtr.name("error").value("email");
                stat = HttpStatus.ACCEPTED;
            } else {
                if(body.emailSt) {
                    String code = UUID.randomUUID().toString();
                    datas.getEmailService().sendRecCode(body.email, code, empLogin ? "Изменение пароля в EJournal" : "Восстановление пароля в EJournal");
                    settingUser.setEmailCode(code);
                    Instant after = Instant.now().plus(Duration.ofDays(1));
                    Date dateAfter = Date.from(after);
                    settingUser.setExpDateEC(Main.df.format(dateAfter));
                    datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                } else {
                    user.setPassword(body.nPar);
                    datas.getDbService().getUserRepository().saveAndFlush(user);
                }
                stat = HttpStatus.OK;
            }
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: отправляет настройки клиенту
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getSettings")
    public ResponseEntity<JsonObject> getSettings(CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init("", "[GET] /getSettings");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getSettings() != null) {
            SettingUser settingUser = user.getSettings();
            wrtr.name("checkbox_hints").value(settingUser.getHints())
                .name("checkbox_notify").value(settingUser.getNotif())
                .name("checkbox_notify_sched").value(settingUser.getNChangeShedule())
                .name("checkbox_notify_marks").value(settingUser.getNNewMarks())
                .name("checkbox_notify_yo").value(settingUser.getNNewNewsYO())
                .name("checkbox_notify_por").value(settingUser.getNNewNewsPor())
                .name("checkbox_notify_new_sch").value(settingUser.getNNewReqSch());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые SettingsController в методах
     * @see SettingsController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataSettings {
        public String secFR, nPar, id, email, emailCode, login,
                notifToken, invCod, valString;
        public int valInt;
        public boolean val, emailSt;
    }
}