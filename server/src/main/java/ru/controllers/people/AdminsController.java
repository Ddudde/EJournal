package ru.controllers.people;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.admin.AdminsInnerDTO;
import ru.data.DTO.controller.people.admin.AdminsOutDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.people.IAdminsService;

import java.util.Map;

/** RU: Контроллер для раздела управления/просмотра администраторов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AdminsController">http://localhost:9001/swagger/htmlSwag/#/AdminsController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/admins")
@RequiredArgsConstructor
@RestController public class AdminsController {
    private final IDBService dbService;
    private final IAdminsService adminsService;
    private final ISSEService sseService;
    private final IUserService userService;

    /** RU: удаляет у пользователя роль администратора + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/remPep/")
    public ResponseEntity<Void> remPep(@RequestBody AdminsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user1 = dbService.userById(body.id);
        final Syst syst = dbService.getSyst();
        if (syst == null || user1 == null) {
            return ResponseEntity.notFound().build();
        }

        final AdminsOutDTO outDTO = adminsService.deleteRoleUser(user1, syst);
        sseService.sendEventFor("remPepC", outDTO, TypesConnect.ADMINS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chPep/")
    public ResponseEntity<Void> chPep(@RequestBody AdminsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();

        final AdminsOutDTO outDTO = adminsService.changeFIO(user1, body.name);
        sseService.sendEventFor("chPepC", outDTO, TypesConnect.ADMINS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт пользователя-администратора + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PostMapping("/addPep/")
    public ResponseEntity<Void> addPep(@RequestBody AdminsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Syst syst = dbService.getSyst();
        if (syst == null) return ResponseEntity.notFound().build();

        final AdminsOutDTO outDTO = adminsService.addNewAccountWithRole(syst, body.name);
        sseService.sendEventFor("addPepC", outDTO, TypesConnect.ADMINS, "main", "main", "main", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: [start] отправляет список администраторов
     * @see DocsHelpController#point Описание */
    @GetMapping("/getAdmins/")
    public ResponseEntity<Map<Long, UserServiceBodyUserDTO>> getAdmins(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final Syst syst = dbService.getSyst();
        final User user = dbService.userById(sub.getUserId());
        String role = "main";
        if (user != null && user.getRoles().containsKey(Roles.ADMIN)) {
            role = "adm";
        }
        if (syst == null) return ResponseEntity.notFound().build();

        final Map<Long, UserServiceBodyUserDTO> mapUsers = userService.usersByListEntity(syst.getAdmins(), true);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.ADMINS, "null", "main", role, "main");
        return ResponseEntity.ok(mapUsers);
    }
}