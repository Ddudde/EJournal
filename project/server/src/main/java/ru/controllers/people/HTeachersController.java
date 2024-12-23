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
import ru.Main;
import ru.controllers.AuthController;
import ru.controllers.DocsHelpController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.Role;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.School;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static ru.Main.datas;

/** RU: Контроллер для раздела управления/просмотра завучей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/HTeachersController">http://localhost:9001/swagger/htmlSwag/#/HTeachersController</a>
 *
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 *
 * toDo: много кривизны на клиенте. Функции школ и групп.
 * </pre>
 * @see Subscriber */
@RequestMapping("/hteachers")
@RequiredArgsConstructor
@RestController public class HTeachersController {
    private final AuthController authController;

    /** RU: удаляет группу + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remGroup")
    public ResponseEntity<Void> remGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /remGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = datas.getDbService().groupById(body.grId);
        datas.getDbService().getGroupRepository().delete(group);
        school.getGroups().remove(group);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);

        wrtr.name("id").value(group.getId());
        return datas.getObjR(ans -> {
            authController.sendEventFor("remGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт группу + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addGroup")
    public ResponseEntity<Void> addGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = new Group(body.name);
        datas.getDbService().getGroupRepository().saveAndFlush(group);
        school.getGroups().add(group);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);

        wrtr.name("id").value(group.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            authController.sendEventFor("addGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет название группы + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chGroup")
    public ResponseEntity<Void> chGroup(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chGroup");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        final Group group = datas.getDbService().groupById(body.grId);
        group.setName(body.name);
        datas.getDbService().getGroupRepository().saveAndFlush(group);

        wrtr.name("id").value(group.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            authController.sendEventFor("chGroupC", ans, TypesConnect.MAIN, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chPep");
        final User user1 = datas.getDbService().userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRoles().get(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        datas.getDbService().getUserRepository().saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("id1").value(sch.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                authController.sendEventFor("chInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                authController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("chInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.OK);
    }

    /** RU: удаляет у пользователя роль завуча + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /remPep");
        final User user = sub.getUser();
        final User user1 = datas.getDbService().userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();
        final School sch = user1.getRoles().get(Roles.HTEACHER).getYO();
        if (sch == null) return ResponseEntity.notFound().build();

        user1.getRoles().remove(Roles.HTEACHER);
        datas.getDbService().getUserRepository().saveAndFlush(user1);
        sch.getHteachers().remove(user1);
        datas.getDbService().getSchoolRepository().saveAndFlush(sch);

        wrtr.name("id").value(user1.getId())
            .name("id1").value(sch.getId());
        return datas.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                authController.sendEventFor("remInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                authController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("remInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя-завуча + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('ADMIN') or hasAuthority('HTEACHER'))""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addPep");
        final User user = sub.getUser();
        Long schId = body.yo;
        if (user.getSelRole() != Roles.ADMIN) {
            schId = user.getRoles().get(Roles.HTEACHER).getYO().getId();
        }
        final School sch = datas.getDbService().schoolById(schId);
        if (sch == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null, sch));
        final User inv = new User(body.name, Map.of(
            Roles.HTEACHER, role
        ), Main.df.format(dateAfter));
        datas.getDbService().getUserRepository().saveAndFlush(inv);
        sch.getHteachers().add(inv);
        datas.getDbService().getSchoolRepository().saveAndFlush(sch);

        wrtr.name("id1").value(sch.getId())
            .name("id").value(inv.getId())
            .name("body").beginObject()
            .name("name").value(body.name).endObject();
        return datas.getObjR(ans -> {
            if (user.getSelRole() == Roles.ADMIN) {
                authController.sendEventFor("addInfoL2C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sch.getId() + "", "main", "ht", "main");
            }
            if (user.getSelRole() == Roles.HTEACHER) {
                authController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
                authController.sendEventFor("addInfoL2C", ans, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
            }
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: изменение имени учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chSch")
    public ResponseEntity<Void> chSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final School school = datas.getDbService().schoolById(body.schId);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chSch");
        if (school == null) return ResponseEntity.notFound().build();

        school.setName(body.name);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);

        wrtr.name("id").value(body.schId)
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            authController.sendEventFor("chInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: добавление учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PostMapping("/addSch")
    public ResponseEntity<Void> addSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addSch");
        final School school = new School(body.name);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);
        wrtr.name("id").value(school.getId())
            .name("body").beginObject()
            .name("name").value(body.name)
            .endObject();
        return datas.getObjR(ans -> {
            authController.sendEventFor("addInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: удаление учебного центра администратором портала
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/remSch")
    public ResponseEntity<Void> remSch(@RequestBody DataHTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final School school = datas.getDbService().schoolById(body.schId);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /remSch");
        if (school == null) return ResponseEntity.notFound().build();

        datas.getDbService().getSchoolRepository().delete(school);

        wrtr.name("id").value(body.schId);
        return datas.getObjR(ans -> {
            authController.sendEventFor("remInfoL1C", ans, TypesConnect.HTEACHERS, sub.getLvlSch(), "main", sub.getLvlMore1(), "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: отправка списка завучей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfo");
        final School school = user.getSelecRole().getYO();
        if (school == null) return ResponseEntity.notFound().build();

        datas.usersByList(school.getHteachers(), true, wrtr);
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.HTEACHERS, school.getId() + "", "main", "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправка списка завучей учебного центра для администраторов
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @GetMapping("/getInfoFA")
    public ResponseEntity<JsonObject> getInfoForAdmins(CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoFA");
        for (School el : datas.getDbService().getSchools()) {
            wrtr.name(el.getId() + "").beginObject()
                .name("name").value(el.getName())
                .name("pep").beginObject();
            datas.usersByList(el.getHteachers(), true, wrtr);
            wrtr.endObject().endObject();
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.HTEACHERS, "null", "main", "adm", "main");
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