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

/** RU: Контроллер для раздела управления/просмотра родителей группы учебного центра + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ParentsController">http://localhost:9001/swagger/htmlSwag/#/ParentsController</a>
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
@RequestMapping("/parents")
@RequiredArgsConstructor
@RestController public class ParentsController {

    private final AuthController authController;

    /** RU: удаляет роль родителя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataParents body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user1 = datas.getDbService().userById(body.id);
        final JsonTreeWriter wrtr = datas.init("", "[DELETE] /remPep");
        final Group group = datas.getDbService().groupById(Long.parseLong(sub.getLvlGr()));
        if (group == null || user1 == null) {
            return ResponseEntity.notFound().build();
        }
        user1.getRoles().remove(Roles.PARENT);
        datas.getDbService().getUserRepository().saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1);
        datas.getDbService().getGroupRepository().saveAndFlush(group);

        wrtr.name("id").value(user1.getId());
        return datas.getObjR(ans -> {
            authController.sendEventFor("remPepC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО родителю + Server Sent Events
     * Не реализовано в клиенте
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataParents body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user1 = datas.getDbService().userById(body.id);
        final JsonTreeWriter wrtr = datas.init("", "[PATCH] /chPep");
        if (user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        datas.getDbService().getUserRepository().saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return datas.getObjR(ans -> {
            authController.sendEventFor("chPepC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя(лей)-родителя(лей)
     * и сразу прикрепляет к существующему ребёнку без родителей + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPar")
    public ResponseEntity<Void> addPar(@RequestBody DataParents body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final JsonTreeWriter wrtr = datas.init("", "[POST] /addPar");
        final User kidU = datas.getDbService().userById(body.id);
        final JsonObject par = body.bod.getAsJsonObject("par");
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        if (kidU == null) return ResponseEntity.notFound().build();

        final School school = datas.getDbService().schoolById(Long.parseLong(sub.getLvlSch()));
        wrtr.name("id").value(kidU.getId())
            .name("body").beginObject()
            .name("name").value(kidU.getFio())
            .name("login").value(kidU.getUsername())
            .name("par").beginObject();
        for (String id : par.keySet()) {
            final Role role = datas.getDbService().getRoleRepository()
                .saveAndFlush(new Role(null, school));
            final User inv = new User(par.getAsJsonObject(id).get("name").getAsString(), Map.of(
                Roles.PARENT, role
            ), Main.df.format(dateAfter));
            datas.getDbService().getUserRepository().saveAndFlush(inv);

            wrtr.name(inv.getId() + "").beginObject()
                .name("name").value(inv.getFio());

            if (!inv.getRoles().get(Roles.PARENT).getKids().contains(kidU)) {
                inv.getRoles().get(Roles.PARENT).getKids().add(kidU);
            }
            kidU.getRoles().get(Roles.KID).getParents().add(inv);
            datas.getDbService().getUserRepository().saveAndFlush(kidU);

            wrtr.endObject();
        }
        wrtr.endObject().endObject();
        return datas.getObjR(ans -> {
            authController.sendEventFor("addParC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: отправляет список ребёнок-родители группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @GetMapping("/getParents/{grId}")
    public ResponseEntity<JsonObject> getParents(@PathVariable Long grId, @AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getParents");
        final School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = datas.getDbService().getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = datas.getDbService().groupById(grId);
        if (group == null || school == null || !school.getGroups().contains(group)) {
            return ResponseEntity.notFound().build();
        }
        wrtr.name("bodyP").beginObject();
        if (!ObjectUtils.isEmpty(group.getKids())) {
            for (User studU : group.getKids()) {
                if (studU == null) continue;
                wrtr.name(studU.getId() + "").beginObject()
                    .name("name").value(studU.getFio());
                if (!ObjectUtils.isEmpty(studU.getUsername())) {
                    wrtr.name("login").value(studU.getUsername());
                }
                if (!ObjectUtils.isEmpty(studU.getCode())) {
                    wrtr.name("link").value(studU.getCode());
                }
                wrtr.name("par").beginObject();
                datas.usersByList(studU.getRoles().get(Roles.KID).getParents(), true, wrtr);
                wrtr.endObject().endObject();
            }
        }
        wrtr.endObject().name("bodyC").beginObject();
        datas.usersByList(group.getKids(), true, wrtr);
        wrtr.endObject();
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, null, school.getId() + "", group.getId() + "", null, null);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] запускает клиента в раздел Родители и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        System.out.println("[GET] /getInfo");
        authController.infCon(auth.getUUID(), null, TypesConnect.PARENTS, "main", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп учебного центра и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfoFH")
    public ResponseEntity<JsonObject> getInfoForHTeacher(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfo");
        wrtr.name("bodyG").beginObject();
        final Long firstG = datas.groupsBySchoolOfUser(user, wrtr);
        wrtr.name("firstG").value(firstG);
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.PARENTS, "main", "main", "ht", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые ParentsController в методах
     * @see ParentsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataParents {
        public final String name;
        public final Long id;
        public final JsonObject bod;
    }
}