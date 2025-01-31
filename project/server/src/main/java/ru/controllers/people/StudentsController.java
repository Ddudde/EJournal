package ru.controllers.people;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.controllers.TypesConnect;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.GroupRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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
    private final UserRepository userRepository;
    private final DBService dbService;
    private final GroupRepository groupRepository;
    private final MainService mainService;
    private final RoleRepository roleRepository;

    /** RU: удаляет роль ученика у пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataStudents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[DELETE] /remPep");
        final User user1 = dbService.userById(body.id);
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (user1 == null || group == null) {
            return ResponseEntity.notFound().build();
        }
        user1.getRoles().remove(Roles.HTEACHER);
        userRepository.saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) {
            group.getKids().remove(user1);
        }
        groupRepository.saveAndFlush(group);

        wrtr.name("id").value(user1.getId());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("remPepC", ans, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО ученика + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataStudents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[PATCH] /chPep");
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        userRepository.saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chPepC", ans, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя-ученика и отправляет информацию + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataStudents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[POST] /addPep");
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (group == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final School school = dbService.schoolById(Long.parseLong(sub.getLvlSch()));
        final Role role = roleRepository.saveAndFlush(new Role(null, school, group));
        final User inv = new User(body.name, Map.of(
            Roles.KID, role
            ), MainService.df.format(dateAfter));
        userRepository.saveAndFlush(inv);
        group.getKids().add(inv);
        groupRepository.saveAndFlush(group);

        wrtr.name("id").value(inv.getId())
            .name("body").beginObject()
            .name("name").value(body.name).endObject();
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addPepC", ans, TypesConnect.STUDENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: отправляет список учеников группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getStud/{grId}")
    public ResponseEntity<JsonObject> getStud(@PathVariable Long grId, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getStud");
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = dbService.getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = dbService.groupById(grId);
        if (group != null && school != null && school.getGroups().contains(group)) {
            mainService.usersByList(group.getKids(), true, wrtr);
        }
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, null, school.getId() + "", group.getId() + "", null, null);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] запускает клиента в раздел Одноклассники и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        log.info("[GET] /getInfo");
        SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.STUDENTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп учебного центра и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfoFH")
    public ResponseEntity<JsonObject> getInfoForHTeacher(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfoFH");
        wrtr.name("bodyG").beginObject();
        final Long firstG = mainService.groupsBySchoolOfUser(user, wrtr);
        wrtr.name("firstG").value(firstG);
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.STUDENTS, "main", "main", "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые StudentsController в методах
     * @see StudentsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataStudents {
        public final String name;
        public final Long id;
    }
}