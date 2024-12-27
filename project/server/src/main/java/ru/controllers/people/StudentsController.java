package ru.controllers.people;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
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

/** RU: Контроллер для раздела управления/просмотра учеников группы учебного центра + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/StudentsController">http://localhost:9001/swagger/htmlSwag/#/StudentsController</a>
 *
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 *
 * </pre>
 * @see Subscriber */
@RequestMapping("/students")
@RequiredArgsConstructor
@RestController public class StudentsController {

    private final AuthController authController;

    /** RU: удаляет роль ученика у пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataStudents body, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[DELETE] /remPep");
        final User user1 = datas.getDbService().userById(body.id);
        final Group group = datas.getDbService().groupById(Long.parseLong(auth.getSub().getLvlGr()));
        if (user1 == null || group == null) {
            return ResponseEntity.notFound().build();
        }
        user1.getRoles().remove(Roles.HTEACHER);
        datas.getDbService().getUserRepository().saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) {
            group.getKids().remove(user1);
        }
        datas.getDbService().getGroupRepository().saveAndFlush(group);

        wrtr.name("id").value(user1.getId());
        return datas.getObjR(ans -> {
            authController.sendEventFor("remPepC", ans, TypesConnect.STUDENTS, auth.getSub().getLvlSch(), auth.getSub().getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО ученика + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataStudents body, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[PATCH] /chPep");
        final User user1 = datas.getDbService().userById(body.id);
        if (user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        datas.getDbService().getUserRepository().saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            authController.sendEventFor("chPepC", ans, TypesConnect.STUDENTS, auth.getSub().getLvlSch(), auth.getSub().getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя-ученика и отправляет информацию + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataStudents body, CustomToken auth) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[POST] /addPep");
        final Group group = datas.getDbService().groupById(Long.parseLong(auth.getSub().getLvlGr()));
        if (group == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final School school = datas.getDbService().schoolById(Long.parseLong(auth.getSub().getLvlSch()));
        final Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role(null, school, group));
        final User inv = new User(body.name, Map.of(
            Roles.KID, role
            ), Main.df.format(dateAfter));
        datas.getDbService().getUserRepository().saveAndFlush(inv);
        group.getKids().add(inv);
        datas.getDbService().getGroupRepository().saveAndFlush(group);

        wrtr.name("id").value(inv.getId())
            .name("body").beginObject()
            .name("name").value(body.name).endObject();
        return datas.getObjR(ans -> {
            authController.sendEventFor("addPepC", ans, TypesConnect.STUDENTS, auth.getSub().getLvlSch(), auth.getSub().getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: отправляет список учеников группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#auth.getSub().getUser() != null)")
    @GetMapping("/getStud/{grId}")
    public ResponseEntity<JsonObject> getStud(@PathVariable Long grId, CustomToken auth) throws Exception {
        final User user = auth.getSub().getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getStud");
        final School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = datas.getDbService().getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = datas.getDbService().groupById(grId);
        if (group != null && school != null && school.getGroups().contains(group)) {
            datas.usersByList(group.getKids(), true, wrtr);
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, null, school.getId() + "", group.getId() + "", null, null);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] запускает клиента в раздел Одноклассники и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(CustomToken auth) throws Exception {
        System.out.println("[GET] /getInfo");
        authController.infCon(auth.getUUID(), null, TypesConnect.STUDENTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп учебного центра и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#auth.getSub().getUser() != null)
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfoFH")
    public ResponseEntity<JsonObject> getInfoForHTeacher(CustomToken auth) throws Exception {
        final User user = auth.getSub().getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoFH");
        wrtr.name("bodyG").beginObject();
        final Long firstG = datas.groupsBySchoolOfUser(user, wrtr);
        wrtr.name("firstG").value(firstG);
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.STUDENTS, "main", "main", "ht", "main");
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