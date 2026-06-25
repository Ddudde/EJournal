package ru.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.configs.SecurityConfig;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;
import ru.data.DTO.controller.auth.AuthServiceDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.IAuthService;
import ru.services.interfaces.logic.ISSEService;

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
    public ResponseEntity<AuthOutDTO> infCon(@RequestBody AuthInnerDTO body, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        sseService.changeSubscriber(auth.getUUID(), body.type, null, null, null, null);
        if(user == null) return ResponseEntity.ok().build();

        final AuthOutDTO outDTO = authService.prepareConnection(body, user);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: завершение сеанса
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(#sub != null)")
    @PatchMapping("/remCon")
    public ResponseEntity<Void> closeConnetion(@AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        sseService.onCloseSSE(sub, UUID.fromString(auth.getUUID()), "remCon", auth.getUserId());
        return ResponseEntity.ok().build();
    }

    /** RU: авторизация пользователя
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserByAuth(#auth))")
    @PostMapping("/auth")
    public ResponseEntity<AuthServiceDTO> auth(@RequestBody AuthInnerDTO body, AuthToken auth) {
        log.trace("auth!" + auth.toString());
        final User user = dbService.userById(auth.getUserId());

        final AuthServiceDTO outDTO = authService.authUser(body, auth, user);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, outDTO.cookie).body(outDTO);
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

    /** RU: проверка заранее выданного рефреш токена и выдача нового вместе с JWT
     * @see DocsHelpController#point Описание */
    @GetMapping("/refreshToken")
    public ResponseEntity<AuthServiceDTO> refreshToken(@CookieValue(value = SecurityConfig.NAME_OF_COOKIE) String token) {
        final AuthServiceDTO outDTO = authService.validateRefresh(token);
        if(outDTO == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, outDTO.cookie).body(outDTO);
    }

    /** RU: установка/обновление инвайта для регистрации + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/setCodePep")
    public ResponseEntity<AuthOutDTO> setCodePep(@RequestBody AuthInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userByLogin(body.id);
        if(user1 == null) return ResponseEntity.notFound().build();

        final Long schId = dbService.getFirstRole(user1.getRoles()).getYO().getId();

        final AuthOutDTO outDTO = authService.setupInviteCode(user1, schId);
        sseService.sendEventFor(auth.getUserId(), "codPepL2C", outDTO, sub.getType(), "null", sub.getLvlGr(), "adm", "main");
        sseService.sendEventFor(auth.getUserId(), "codPepL1C", outDTO, sub.getType(), schId +"", sub.getLvlGr(), "ht", "main");
        return ResponseEntity.ok(outDTO);
    }

}