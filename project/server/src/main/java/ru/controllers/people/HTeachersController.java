package ru.controllers.people;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

/** RU: Контроллер для раздела управления/просмотра завучей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/HTeachersController">http://localhost:9001/swagger/htmlSwag/#/HTeachersController</a>
 * toDo: много кривизны на клиенте. Функции школ и групп.
 * </pre>
 * @see SubscriberDTO */
@RequestMapping("/hteachers")
@RequiredArgsConstructor
@RestController public class HTeachersController {
    private final SchoolRepository schoolRepository;
    private final GroupRepository groupRepository;
    private final DBService dbService;
    private final MainService mainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /** RU: удаляет группу + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remGroup")
    public ResponseEntity<Void> remGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[DELETE] /remGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = dbService.groupById(body.grId);
        groupRepository.delete(group);
        school.getGroups().remove(group);
        schoolRepository.saveAndFlush(school);

        wrtr.name("id").value(group.getId());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("remGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт группу + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addGroup")
    public ResponseEntity<Void> addGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = new Group(body.name);
        groupRepository.saveAndFlush(group);
        school.getGroups().add(group);
        schoolRepository.saveAndFlush(school);

        wrtr.name("id").value(group.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет название группы + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chGroup")
    public ResponseEntity<Void> chGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = dbService.groupById(body.grId);
        group.setName(body.name);
        groupRepository.saveAndFlush(group);

        wrtr.name("id").value(group.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chPep");
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRole(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        userRepository.saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("id1").value(sch.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                SSEController.sendEventFor("chInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                SSEController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("chInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.OK);
    }

    /** RU: удаляет у пользователя роль завуча + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[DELETE] /remPep");
        final User user = dbService.userById(sub.getUserId());
        final User user1 = dbService.userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRoles().get(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        user1.getRoles().remove(Roles.HTEACHER);
        userRepository.saveAndFlush(user1);
        sch.getHteachers().remove(user1);
        schoolRepository.saveAndFlush(sch);

        wrtr.name("id").value(user1.getId())
            .name("id1").value(sch.getId());
        return mainService.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                SSEController.sendEventFor("remInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                SSEController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("remInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя-завуча + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addPep");
        final User user = dbService.userById(sub.getUserId());
        Long schId = body.yo;
        if (user.getSelRole() != Roles.ADMIN) {
            schId = user.getRole(Roles.HTEACHER).getYO().getId();
        }
        final School sch = dbService.schoolById(schId);
        if (sch == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = roleRepository.saveAndFlush(new Role(null, sch));
        final User inv = new User(body.name, Map.of(
            Roles.HTEACHER, role
        ), MainService.df.format(dateAfter));
        userRepository.saveAndFlush(inv);
        sch.getHteachers().add(inv);
        schoolRepository.saveAndFlush(sch);

        wrtr.name("id1").value(sch.getId())
            .name("id").value(inv.getId())
            .name("body").beginObject()
            .name("name").value(body.name).endObject();
        return mainService.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                SSEController.sendEventFor("addInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                SSEController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                SSEController.sendEventFor("addInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: изменение имени учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chSch")
    public ResponseEntity<Void> chSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final School school = dbService.schoolById(body.schId);
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[PATCH] /chSch");
        if (school == null) return ResponseEntity.notFound().build();

        school.setName(body.name);
        schoolRepository.saveAndFlush(school);

        wrtr.name("id").value(body.schId)
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: добавление учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @PostMapping("/addSch")
    public ResponseEntity<Void> addSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addSch");
        final School school = new School(body.name);
        schoolRepository.saveAndFlush(school);
        wrtr.name("id").value(school.getId())
            .name("body").beginObject()
            .name("name").value(body.name)
            .endObject();
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: удаление учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/remSch")
    public ResponseEntity<Void> remSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final School school = dbService.schoolById(body.schId);
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[DELETE] /remSch");
        if (school == null) return ResponseEntity.notFound().build();

        schoolRepository.delete(school);

        wrtr.name("id").value(body.schId);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: отправка списка завучей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfo");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        mainService.usersByList(school.getHteachers(), true, wrtr);
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.HTEACHERS, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправка списка завучей учебного центра для администраторов
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('ADMIN')""")
    @GetMapping("/getInfoFA")
    public ResponseEntity<JsonObject> getInfoForAdmins(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfoFA");
        for (School el : dbService.getSchools()) {
            wrtr.name(el.getId() + "").beginObject()
                .name("name").value(el.getName())
                .name("pep").beginObject();
            mainService.usersByList(el.getHteachers(), true, wrtr);
            wrtr.endObject().endObject();
        }
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые HTeachersController в методах
     * @see HTeachersController */
    @ToString
    @RequiredArgsConstructor
    static final class DataHTeachers {
        public final String name;
        public final Long schId, yo, id, grId;
    }
}