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
import ru.data.DTO.controller.people.parents.ParentsInnerDTO;
import ru.data.DTO.controller.people.parents.ParentsOutDTO;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.data.IGroupService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.people.IParentsService;

/** RU: Контроллер для раздела управления/просмотра родителей группы учебного центра + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ParentsController">http://localhost:9001/swagger/htmlSwag/#/ParentsController</a>
 </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/parents")
@RequiredArgsConstructor
@RestController public class ParentsController {
    private final IDBService dbService;
    private final IParentsService parentsService;
    private final IGroupService groupService;
    private final ISSEService sseService;

    /** RU: удаляет роль родителя + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody ParentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (group == null || user1 == null) {
            return ResponseEntity.notFound().build();
        }

        final ParentsOutDTO outDTO = parentsService.deleteRoleUser(user1, group);
        sseService.sendEventFor(auth.getUserId(), "remPepC", outDTO, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: изменяет ФИО родителю + Server Sent Events
     * Не реализовано в клиенте
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody ParentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();

        final ParentsOutDTO outDTO = parentsService.changeFIO(user1, body.name);
        sseService.sendEventFor(auth.getUserId(), "chPepC", outDTO, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: создаёт пользователя(лей)-родителя(лей)
     * и сразу прикрепляет к существующему ребёнку без родителей + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPar")
    public ResponseEntity<Void> addPar(@RequestBody ParentsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub, AuthToken auth) {
        final User kidU = dbService.userById(body.id);
        if (kidU == null) return ResponseEntity.notFound().build();

        final ParentsOutDTO outDTO = parentsService.addNewAccountWithRole(kidU, body, sub);
        sseService.sendEventFor(auth.getUserId(), "addParC", outDTO, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: отправляет список ребёнок-родители группы
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserByAuth(#auth))")
    @GetMapping("/getParents/{grId}")
    public ResponseEntity<ParentsOutDTO> getParents(@PathVariable Long grId, AuthToken auth) {
        final User user = dbService.userById(auth.getUserId());
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = dbService.getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = dbService.groupById(grId);
        if (group == null || school == null || !school.getGroups().contains(group)) {
            return ResponseEntity.notFound().build();
        }

        final ParentsOutDTO outDTO = parentsService.prepareParents(group);
        sseService.changeSubscriber(auth.getUUID(), null, school.getId() + "", group.getId() + "", null, null);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] запускает клиента в раздел Родители и подтверждает клиенту права
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserByAuth(#auth))
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(AuthToken auth) {
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.PARENTS, "main", "main", "main", "main");
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
        sseService.changeSubscriber(auth.getUUID(), TypesConnect.PARENTS, "main", "main", "ht", "main");
        return ResponseEntity.ok(outDTO);
    }

}