package ru.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.IAuthService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.logic.SSEService;

import java.util.UUID;

/** RU: Контроллер для раздела авторизации
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AuthController">http://localhost:9001/swagger/htmlSwag/#/AuthController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final IDBService dbService;
    private final IAuthService authService;
    private final ISSEService sseService;

    /** RU: [start] изменение подписки
     * @see DocsHelpController#point Описание */
    @PatchMapping("/infCon")
    public ResponseEntity<AuthOutDTO> infCon(@RequestBody AuthInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userByLogin(body.login);
        sseService.changeSubscriber(auth.getUUID(), body.login, body.type, null, null, null, null);
        if(user == null) return ResponseEntity.ok().build();

        final AuthOutDTO outDTO = authService.prepareConnection(body, sub, user);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: завершение сеанса
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(#sub != null)")
    @PatchMapping("/remCon")
    public ResponseEntity<Void> remCon(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        if(sub.getLogin() != null) {
            log.debug("subscription remCon " + auth.getUUID() + " was noclosed " + sub.getLogin());
        } else {
            SSEService.subscriptions.remove(UUID.fromString(auth.getUUID()));
            log.debug("subscription remCon " + auth.getUUID() + " was closed");
        }
        sub.getSSE().complete();
        return ResponseEntity.ok().build();
    }

    /** RU: авторизация пользователя
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PostMapping("/auth")
    public ResponseEntity<AuthOutDTO> auth(@RequestBody AuthInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());

        final AuthOutDTO outDTO = authService.authUser(body, auth.getUUID(), user);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: регистрация пользователя
     * @see DocsHelpController#point Описание */
    @PostMapping("/reg")
    public ResponseEntity<AuthOutDTO> reg(@RequestBody AuthInnerDTO body) {
        final User user = dbService.userByLogin(body.login),
            user1 = dbService.userByCode(body.code);
        if(user != null) return ResponseEntity.notFound().build();

        final AuthOutDTO outDTO = authService.createUser(user1, body);
        if(outDTO != null) return ResponseEntity.accepted().body(outDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: проверка инвайта для регистрации/регистрации новой роли
     * @see DocsHelpController#point Описание */
    @PostMapping("/checkInvCode")
    public ResponseEntity<Void> checkInvCode(@RequestBody AuthInnerDTO body) {
        final User user = dbService.userByCode(body.code);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/setCodePep")
    public ResponseEntity<AuthOutDTO> setCodePep(@RequestBody AuthInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user1 = dbService.userByLogin(body.id);
        if(user1 == null) return ResponseEntity.notFound().build();
        final Long schId = dbService.getFirstRole(user1.getRoles()).getYO().getId();

        final AuthOutDTO outDTO = authService.setupInviteCode(user1, schId);
        sseService.sendEventFor("codPepL2C", outDTO, sub.getType(), "null", sub.getLvlGr(), "adm", "main");
        sseService.sendEventFor("codPepL1C", outDTO, sub.getType(), schId +"", sub.getLvlGr(), "ht", "main");
        return ResponseEntity.ok(outDTO);
    }

}