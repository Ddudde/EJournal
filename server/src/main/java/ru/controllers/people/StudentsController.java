package ru.controllers.people;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.students.StudentsInnerDTO;
import ru.data.DTO.controller.people.students.StudentsOutDTO;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.data.IGroupService;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.people.IStudentsService;

import java.util.Map;

/** RU: Контроллер для раздела управления/просмотра учеников группы учебного центра + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/StudentsController">http://localhost:9001/swagger/htmlSwag/#/StudentsController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/students")
@RequiredArgsConstructor
@RestController public class StudentsController {
    private final IDBService dbService;
    private final IStudentsService studentsService;
    private final ISSEService sseService;
    private final IUserService userService;
    private final IGroupService groupService;

    /** RU: удаляет роль ученика у пользователя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody StudentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (user1 == null || group == null) {
            return ResponseEntity.notFound().build();
        }

        final StudentsOutDTO outDTO = studentsService.deleteRoleUser(user1, group);
        sseService.sendEventFor(auth.getUserId(), "remPepC", outDTO, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет ФИО ученика + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody StudentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();

        final StudentsOutDTO outDTO = studentsService.changeFIO(user1, body.name);
        sseService.sendEventFor(auth.getUserId(), "chPepC", outDTO, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт пользователя-ученика и отправляет информацию + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody StudentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (group == null) return ResponseEntity.notFound().build();

        final StudentsOutDTO outDTO = studentsService.addNewAccountWithRole(Long.parseLong(sub.getLvlSch()), group, body.name);
        sseService.sendEventFor(auth.getUserId(), "addPepC", outDTO, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: отправляет список учеников группы
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserByAuth(#auth))")
    @GetMapping("/getStud/{grId}")
    public ResponseEntity<Map<Long, UserServiceBodyUserDTO>> getStud(@PathVariable Long grId, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = dbService.getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = dbService.groupById(grId);
        if (group == null || school == null || !school.getGroups().contains(group)) {
            return ResponseEntity.notFound().build();
        }

        final Map<Long, UserServiceBodyUserDTO> outDTO = userService.usersByListEntity(group.getKids(), true);
        sseService.changeSubscriber(auth.getUUID(), null, school.getId() + "", group.getId() + "", null, null);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] запускает клиента в раздел Одноклассники и подтверждает клиенту права
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(AuthToken auth) {
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.STUDENTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп учебного центра и подтверждает клиенту права
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfoFH")
    public ResponseEntity<GroupServiceDTO> getInfoForHTeacher(AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());

        final GroupServiceDTO outDTO = groupService.groupsBySchoolOfUser(user);
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.STUDENTS, "main", "main", "ht", "main");
        return ResponseEntity.ok(outDTO);
    }
}