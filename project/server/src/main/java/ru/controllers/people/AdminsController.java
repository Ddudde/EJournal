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
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static ru.Main.datas;

/** RU: Контроллер для раздела управления/просмотра администраторов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/AdminsController">http://localhost:9001/swagger/htmlSwag/#/AdminsController</a>
 * </pre>
 * @see Subscriber */
@RequestMapping("/admins")
@RequiredArgsConstructor
@RestController public class AdminsController {

    /** RU: удаляет у пользователя роль администратора + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataAdmins body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user1 = datas.getDbService().userById(body.id);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /remPep");
        final Syst syst = datas.getDbService().getSyst();
        if (syst == null || user1 == null) {
            return ResponseEntity.notFound().build();
        }

        user1.getRoles().remove(Roles.ADMIN);
        datas.getDbService().getUserRepository().saveAndFlush(user1);
        syst.getAdmins().remove(user1);
        datas.getDbService().getSystRepository().saveAndFlush(syst);

        wrtr.name("id").value(user1.getId());
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("remPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataAdmins body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user1 = datas.getDbService().userById(body.id);
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chPep");
        if (user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        datas.getDbService().getUserRepository().saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("chPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя-администратора + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('ADMIN')""")
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataAdmins body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final Syst syst = datas.getDbService().getSyst();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addPep");
        if (syst == null) return ResponseEntity.notFound().build();

        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        final Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role());
        final User inv = new User(body.name, Map.of(
            Roles.ADMIN, role
            ), Main.df.format(dateAfter));
        datas.getDbService().getUserRepository().saveAndFlush(inv);
        syst.getAdmins().add(inv);
        datas.getDbService().getSystRepository().saveAndFlush(syst);

        wrtr.name("id").value(inv.getId())
            .name("body").beginObject()
            .name("name").value(body.name)
            .endObject();
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("addPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: [start] отправляет список администраторов
     * @see DocsHelpController#point(Object, Object) Описание */
    @GetMapping("/getAdmins")
    public ResponseEntity<JsonObject> getAdmins(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final Syst syst = datas.getDbService().getSyst();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getAdmins");
        if (syst == null) return ResponseEntity.notFound().build();

        datas.usersByList(syst.getAdmins(), true, wrtr);
        return datas.getObjR(ans -> {
            final User user = sub.getUser();
            String role = "main";
            if (user.getRoles().containsKey(Roles.ADMIN)) {
                role = "adm";
            }
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.ADMINS, "null", "main", role, "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые AdminsController в методах
     * @see AdminsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataAdmins {
        public final String name;
        public final Long id;
    }
}