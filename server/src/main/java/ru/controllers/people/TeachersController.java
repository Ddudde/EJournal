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
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.teacher.TeacherOutDTO;
import ru.data.DTO.controller.people.teacher.TeachersInnerDTO;
import ru.data.DTO.service.school.TeacherServiceDTO;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.people.ITeacherService;

/** RU: Контроллер для управления/просмотра преподавателей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TeachersController">http://localhost:9001/swagger/htmlSwag/#/TeachersController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/teachers")
@RequiredArgsConstructor
@RestController public class TeachersController {
    private final IDBService dbService;
    private final ITeacherService teacherService;
    private final ISSEService sseService;

    /** RU: удаление роли преподавателя
     * Не реализовано в клиенте.
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody TeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if(user1 == null || group == null) {
            return ResponseEntity.notFound().build();
        }

        final TeacherOutDTO outDTO = teacherService.deleteRoleUser(user1, group);
        sseService.sendEventFor(auth.getUserId(), "remPepC", outDTO, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет ФИО преподавателю учебного центра.
     * Не реализовано в клиенте.
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody TeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        if(user1 == null) return ResponseEntity.notFound().build();

        final TeacherOutDTO outDTO = teacherService.changeFIO(user1, body.name);
        sseService.sendEventFor(auth.getUserId(), "chPepC", outDTO, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт нового учителя для учебного центра
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addTea")
    public ResponseEntity<Void> addTea(@RequestBody TeachersInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final School school = dbService.schoolById(Long.parseLong(sub.getLvlSch()));
        if(school == null) return ResponseEntity.notFound().build();

        final TeacherOutDTO outDTO = teacherService.addNewAccountWithRole(school, body.name);
        sseService.sendEventFor(auth.getUserId(), "addTeaC", outDTO, TypesConnect.TEACHERS, sub.getLvlSch(), "main", "ht", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: [start] отправка списка учителей учебного центра
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserByAuth(#auth))")
    @GetMapping("/getTeachers")
    public ResponseEntity<TeacherServiceDTO> getTeachers(AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = user.getSelecRole().getYO();
        String role = "main";
        if(user.getRoles().containsKey(Roles.HTEACHER)) {
            role = "ht";
        }
        if(school == null) return ResponseEntity.notFound().build();

        final TeacherServiceDTO outDTO = teacherService.teachersBySchool(school);
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.TEACHERS, school.getId()+"", "main", role, "main");
        return ResponseEntity.ok(outDTO);
    }

}