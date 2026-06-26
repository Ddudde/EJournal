package ru.controllers.people;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.hTeacher.HTeachersBodyDTO;
import ru.data.DTO.controller.people.hTeacher.HTeachersInnerDTO;
import ru.data.DTO.controller.people.hTeacher.HTeachersOutDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.people.IHTeachersService;

import java.util.Map;

/** RU: Контроллер для раздела управления/просмотра завучей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/HTeachersController">http://localhost:9001/swagger/htmlSwag/#/HTeachersController</a>
 * toDo: много кривизны на клиенте. Функции школ и групп.
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/hteachers")
@RequiredArgsConstructor
@RestController public class HTeachersController {
    private final IDBService dbService;
    private final IHTeachersService hTeachersService;
    private final ISSEService sseService;
    private final IUserService userService;

    /** RU: удаляет группу + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remGroup")
    public ResponseEntity<Void> remGroup(@RequestBody HTeachersInnerDTO body, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.removeGroup(body.grId, school);
        sseService.sendEventFor(auth.getUserId(), "remGroupC", outDTO, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт группу + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addGroup")
    public ResponseEntity<Void> addGroup(@RequestBody HTeachersInnerDTO body, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.addGroup(body.name, school);
        sseService.sendEventFor(auth.getUserId(), "addGroupC", outDTO, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: изменяет название группы + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chGroup")
    public ResponseEntity<Void> chGroup(@RequestBody HTeachersInnerDTO body, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.changeNameGroup(body.name, dbService.groupById(body.grId));
        sseService.sendEventFor(auth.getUserId(), "chGroupC", outDTO, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRole(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.changeFIO(user1, body.name, sch.getId());
        if (user.getSelRole() == Roles.ADMIN) {
            sseService.sendEventFor(auth.getUserId(), "chInfoL2C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "chInfoL1C", outDTO, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
        }
        if (user.getSelRole() == Roles.HTEACHER) {
            sseService.sendEventFor(auth.getUserId(), "chInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "chInfoL2C", outDTO, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
        }
        return ResponseEntity.ok().build();
    }

    /** RU: удаляет у пользователя роль завуча + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRoles().get(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.deleteRoleUser(user1, sch);
        if (user.getSelRole() == Roles.ADMIN) {
            sseService.sendEventFor(auth.getUserId(), "remInfoL2C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "remInfoL1C", outDTO, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
        }
        if (user.getSelRole() == Roles.HTEACHER) {
            sseService.sendEventFor(auth.getUserId(), "remInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "remInfoL2C", outDTO, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
        }
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт пользователя-завуча + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        Long schId = body.yo;
        if (user.getSelRole() != Roles.ADMIN) {
            schId = user.getRole(Roles.HTEACHER).getYO().getId();
        }
        final School sch = dbService.schoolById(schId);
        if (sch == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.addNewAccountWithRole(body.name, sch);
        if (user.getSelRole() == Roles.ADMIN) {
            sseService.sendEventFor(auth.getUserId(), "addInfoL2C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "addInfoL1C", outDTO, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
        }
        if (user.getSelRole() == Roles.HTEACHER) {
            sseService.sendEventFor(auth.getUserId(), "addInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
            sseService.sendEventFor(auth.getUserId(), "addInfoL2C", outDTO, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: изменение имени учебного центра администратором портала
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chSch")
    public ResponseEntity<Void> chSch(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final School school = dbService.schoolById(body.schId);
        if (school == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.changeNameSchool(body.name, school);
        sseService.sendEventFor(auth.getUserId(), "chInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        return ResponseEntity.ok().build();
    }

    /** RU: добавление учебного центра администратором портала
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('ADMIN')""")
    @PostMapping("/addSch")
    public ResponseEntity<Void> addSch(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final HTeachersOutDTO outDTO = hTeachersService.addSchool(body.name);

        sseService.sendEventFor(auth.getUserId(), "addInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: удаление учебного центра администратором портала
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/remSch")
    public ResponseEntity<Void> remSch(@RequestBody HTeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final School school = dbService.schoolById(body.schId);
        if (school == null) return ResponseEntity.notFound().build();

        final HTeachersOutDTO outDTO = hTeachersService.deleteSchool(school, body.schId);
        sseService.sendEventFor(auth.getUserId(), "remInfoL1C", outDTO, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        return ResponseEntity.ok().build();
    }

    /** RU: отправка списка завучей учебного центра
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserByAuth(#auth))")
    @GetMapping("/getInfo")
    public ResponseEntity<Map<Long, UserServiceBodyUserDTO>> getInfo(AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Map<Long, UserServiceBodyUserDTO> bodysUserDTO = userService.usersByListEntity(school.getHteachers(), true);
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.HTEACHERS, school.getId() + "", "main", "ht", "main");
        return ResponseEntity.ok(bodysUserDTO);
    }

    /** RU: [start] отправка списка завучей учебного центра для администраторов
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getInfoFA")
    public ResponseEntity<Map<Long, HTeachersBodyDTO>> getInfoForAdmins(AuthToken auth) {
        final Map<Long, HTeachersBodyDTO> outDTO = hTeachersService.prepareInfoForAdmins(dbService.getSchools());

        sseService.changeSubscriber(auth.getUUID(), TypesConnect.HTEACHERS, "null", "main", "adm", "main");
        return ResponseEntity.ok(outDTO);
    }

}