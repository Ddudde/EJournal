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
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.main.profile.ProfileInnerDTO;
import ru.data.DTO.controller.main.profile.ProfileOutDTO;
import ru.data.DTO.service.data.UserServiceDTO;
import ru.security.user.CustomToken;
import ru.services.data.IUserService;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.main.IProfileService;

/** RU: Контроллер для раздела профиля и частично управлением аккаунтом + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ProfileController">http://localhost:9001/swagger/htmlSwag/#/ProfileController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/profiles")
@RequiredArgsConstructor
@RestController public class ProfileController {
    private final IDBService dbService;
    private final IProfileService profileService;
    private final IUserService userService;
    private final ISSEService sseService;

    /** RU: изменение контроллируемого ученика у родителя
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('PARENT')""")
    @PatchMapping("/chKid")
    public ResponseEntity<ProfileOutDTO> chKid(@RequestBody ProfileInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        if(body.idL == null) return ResponseEntity.notFound().build();

        final UserServiceDTO userServiceDTO = userService.setKidIDOfParent(user, body.idL);
        return ResponseEntity.ok()
            .body(profileService.prepareChKidDTO(userServiceDTO));
    }

    /** RU: изменение роли на следующую по иерархии из имеющихся у пользователя
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chRole")
    public ResponseEntity<ProfileOutDTO> chRole(final @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final UserServiceDTO userServiceDTO = userService.setNextRole(user);

        return ResponseEntity.ok()
            .body(profileService.prepareChRoleDTO(userServiceDTO));
    }

    /** RU: выход с аккаунта
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/exit")
    public ResponseEntity<Void> exit(@RequestBody ProfileInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());

        profileService.exitFromAccount(user, sub, body.notifToken);
        return ResponseEntity.ok().build();
    }

    /** RU: изменение/добавление электронной почты определённой роли пользователя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chEmail")
    public ResponseEntity<Void> chEmail(@RequestBody ProfileInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final UserServiceDTO userServiceDTO = userService.changeEmail(user, body.email);

        sseService.sendEventFor("chEmail", userServiceDTO, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        return ResponseEntity.ok().build();
    }

    /** RU: изменение/добавление дополнительной информации о пользователе + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chInfo")
    public ResponseEntity<Void> chInfo(@RequestBody ProfileInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final UserServiceDTO userServiceDTO = userService.changeProfileInfo(user.getSettings(), body.info);

        sseService.sendEventFor("chInfo", userServiceDTO, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        return ResponseEntity.ok().build();
    }

    /** RU: изменение логина пользователя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PatchMapping("/chLogin")
    public ResponseEntity<Void> chLogin(@RequestBody ProfileInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final User userN = dbService.userByLogin(body.nLogin);

        if (userN != null) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        final UserServiceDTO userServiceDTO = userService.changeLogin(user, body.nLogin, sub);
        sseService.sendEventFor("chLogin", userServiceDTO, TypesConnect.PROFILES, "main", "main", "main", body.nLogin);
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет инфу профиля либо другого пользователя либо личную
     * @see DocsHelpController#point Описание */
    @GetMapping({"/getProfile", "/getProfile/{login}"})
    public ResponseEntity<ProfileOutDTO> getProfile(@PathVariable(required = false) String login, CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) {
        User user;
        if(ObjectUtils.isEmpty(login)) {
            user = dbService.userById(sub.getUserId());
        } else {
            user = dbService.userByLogin(login);
        }
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        final ProfileOutDTO outDTO = profileService.getProfileOfUser(user.getSettings(), user);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.PROFILES, "main", "main", "main", user.getUsername());
        return ResponseEntity.ok().body(outDTO);
    }
}