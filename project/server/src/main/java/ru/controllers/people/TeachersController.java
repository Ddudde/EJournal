package ru.controllers.people;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;
import ru.services.db.DBService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/** RU: Контроллер для управления/просмотра преподавателей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TeachersController">http://localhost:9001/swagger/htmlSwag/#/TeachersController</a>
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/teachers")
@RequiredArgsConstructor
@RestController public class TeachersController {
    private final UserRepository userRepository;
    private final DBService dbService;
    private final GroupRepository groupRepository;
    private final MainService mainService;
    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;

    /** RU: удаление роли преподавателя
     * Не реализовано в клиенте.
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[DELETE] /remPep");
        final User user1 = dbService.userById(body.id);
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if(user1 == null || group == null) {
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
            SSEController.sendEventFor("remPepC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО преподавателю учебного центра.
     * Не реализовано в клиенте.
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[PATCH] /chPep");
        final User user1 = dbService.userById(body.id);
        if(user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        userRepository.saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chPepC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт нового учителя для учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addTea")
    public ResponseEntity<Void> addTea(@RequestBody DataTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[POST] /addTea");
        final School school = dbService.schoolById(Long.parseLong(sub.getLvlSch()));
        if(school == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = roleRepository.saveAndFlush(new Role(null, Set.of(), school));
        final User inv = new User(body.name, Map.of(
            Roles.TEACHER, role
            ), MainService.df.format(dateAfter));
        userRepository.saveAndFlush(inv);
        school.getTeachers().add(inv);
        schoolRepository.saveAndFlush(school);

        wrtr.name("id").value(inv.getId());
        wrtr.name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addTeaC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), "main", "ht", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: [start] отправка списка учителей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getTeachers")
    public ResponseEntity<JsonObject> getTeachers(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getTeachers");
        final School school = user.getSelecRole().getYO();
        if(school == null) return ResponseEntity.notFound().build();

        mainService.teachersBySchool(school, wrtr);
        return mainService.getObjR(ans -> {
            String role = "main";
            if(user.getRoles().containsKey(Roles.HTEACHER)) {
                role = "ht";
            }
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.TEACHERS, school.getId()+"", "main", role, "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые TeachersController в методах
     * @see TeachersController */
    @ToString
    @RequiredArgsConstructor
    static final class DataTeachers {
        public final String name;
        public final Long id;
    }
}