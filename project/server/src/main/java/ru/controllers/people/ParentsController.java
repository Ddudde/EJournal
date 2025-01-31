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

/** RU: Контроллер для раздела управления/просмотра родителей группы учебного центра + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ParentsController">http://localhost:9001/swagger/htmlSwag/#/ParentsController</a>
 </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/parents")
@RequiredArgsConstructor
@RestController public class ParentsController {
    private final UserRepository userRepository;
    private final DBService dbService;
    private final GroupRepository groupRepository;
    private final MainService mainService;
    private final RoleRepository roleRepository;

    /** RU: удаляет роль родителя + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataParents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user1 = dbService.userById(body.id);
        final JsonTreeWriter wrtr = mainService.init("", "[DELETE] /remPep");
        final Group group = dbService.groupById(Long.parseLong(sub.getLvlGr()));
        if (group == null || user1 == null) {
            return ResponseEntity.notFound().build();
        }
        user1.getRoles().remove(Roles.PARENT);
        userRepository.saveAndFlush(user1);
        if (!ObjectUtils.isEmpty(group.getKids())) group.getKids().remove(user1);
        groupRepository.saveAndFlush(group);

        wrtr.name("id").value(user1.getId());
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("remPepC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: изменяет ФИО родителю + Server Sent Events
     * Не реализовано в клиенте
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataParents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user1 = dbService.userById(body.id);
        final JsonTreeWriter wrtr = mainService.init("", "[PATCH] /chPep");
        if (user1 == null) return ResponseEntity.notFound().build();

        user1.setFio(body.name);
        userRepository.saveAndFlush(user1);

        wrtr.name("id").value(user1.getId())
            .name("name").value(body.name);
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("chPepC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.OK);
    }

    /** RU: создаёт пользователя(лей)-родителя(лей)
     * и сразу прикрепляет к существующему ребёнку без родителей + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPar")
    public ResponseEntity<Void> addPar(@RequestBody DataParents body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final JsonTreeWriter wrtr = mainService.init("", "[POST] /addPar");
        final User kidU = dbService.userById(body.id);
        final JsonObject par = body.bod.getAsJsonObject("par");
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);
        if (kidU == null) return ResponseEntity.notFound().build();

        final School school = dbService.schoolById(Long.parseLong(sub.getLvlSch()));
        wrtr.name("id").value(kidU.getId())
            .name("body").beginObject()
            .name("name").value(kidU.getFio())
            .name("login").value(kidU.getUsername())
            .name("par").beginObject();
        for (String id : par.keySet()) {
            final Role role = roleRepository.saveAndFlush(new Role(null, school));
            final User inv = new User(par.getAsJsonObject(id).get("name").getAsString(), Map.of(
                Roles.PARENT, role
            ), MainService.df.format(dateAfter));
            userRepository.saveAndFlush(inv);

            wrtr.name(inv.getId() + "").beginObject()
                .name("name").value(inv.getFio());

            if (!inv.getRole(Roles.PARENT).getKids().contains(kidU)) {
                inv.getRole(Roles.PARENT).getKids().add(kidU);
            }
            kidU.getRole(Roles.KID).getParents().add(inv);
            userRepository.saveAndFlush(kidU);

            wrtr.endObject();
        }
        wrtr.endObject().endObject();
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addParC", ans, TypesConnect.PARENTS, sub.getLvlSch(), sub.getLvlGr(), "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: отправляет список ребёнок-родители группы
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getParents/{grId}")
    public ResponseEntity<JsonObject> getParents(@PathVariable Long grId, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getParents");
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        if (!user.getRoles().containsKey(Roles.HTEACHER)) {
            grId = dbService.getFirstRole(user.getRoles()).getGrp().getId();
        }
        final Group group = dbService.groupById(grId);
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
                mainService.usersByList(studU.getRole(Roles.KID).getParents(), true, wrtr);
                wrtr.endObject().endObject();
            }
        }
        wrtr.endObject().name("bodyC").beginObject();
        mainService.usersByList(group.getKids(), true, wrtr);
        wrtr.endObject();
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, null, school.getId() + "", group.getId() + "", null, null);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] запускает клиента в раздел Родители и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and !hasAuthority('ADMIN')""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        log.info("[GET] /getInfo");
        SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.PARENTS, "main", "main", "main", "main");
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
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfo");
        wrtr.name("bodyG").beginObject();
        final Long firstG = mainService.groupsBySchoolOfUser(user, wrtr);
        wrtr.name("firstG").value(firstG);
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.PARENTS, "main", "main", "ht", "main");
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