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
import ru.Main;
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static ru.Main.datas;

/** RU: Контроллер для управления/просмотра преподавателей учебных центров + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TeachersController">http://localhost:9001/swagger/htmlSwag/#/TeachersController</a>
 * </pre>
 * @see Subscriber */
@RequestMapping("/teachers")
@RequiredArgsConstructor
@RestController public class TeachersController {

    /** RU: удаление роли преподавателя
     * Не реализовано в клиенте.
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[DELETE] /remPep");
        final User user1 = datas.getDbService().userById(body.id);
        final Group group = datas.getDbService().groupById(Long.parseLong(sub.getLvlGr()));
        if(user1 == null || group == null) {
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
            SSEController.sendEventFor("remPepC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО преподавателю учебного центра.
     * Не реализовано в клиенте.
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[PATCH] /chPep");
        final User user1 = datas.getDbService().userById(body.id);
        if(user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        datas.getDbService().getUserRepository().saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chPepC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт нового учителя для учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addTea")
    public ResponseEntity<Void> addTea(@RequestBody DataTeachers body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[POST] /addTea");
        final School school = datas.getDbService().schoolById(Long.parseLong(sub.getLvlSch()));
        if(school == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = datas.getDbService().getRoleRepository()
            .saveAndFlush(new Role(null, Set.of(), school));
        final User inv = new User(body.name, Map.of(
            Roles.TEACHER, role
            ), Main.df.format(dateAfter));
        datas.getDbService().getUserRepository().saveAndFlush(inv);
        school.getTeachers().add(inv);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);

        wrtr.name("id").value(inv.getId());
        wrtr.name("name").value(body.name);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("addTeaC", ans, TypesConnect.TEACHERS, sub.getLvlSch(), "main", "ht", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: [start] отправка списка учителей учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @GetMapping("/getTeachers")
    public ResponseEntity<JsonObject> getTeachers(CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getTeachers");
        final School school = user.getSelecRole().getYO();
        if(school == null) return ResponseEntity.notFound().build();

        datas.teachersBySchool(school, wrtr);
        return datas.getObjR(ans -> {
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