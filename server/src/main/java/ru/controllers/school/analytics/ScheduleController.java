package ru.controllers.school.analytics;

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
import ru.data.DTO.controller.school.analytics.schedule.ScheduleInnerDTO;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleOutDTO;
import ru.data.DTO.service.school.ScheduleServiceDTO;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.school.analytics.IScheduleService;

/** RU: Контроллер для управления/просмотра расписания + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ScheduleController">http://localhost:9001/swagger/htmlSwag/#/ScheduleController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/schedule")
@RequiredArgsConstructor
@RestController public class ScheduleController {
    private final IDBService dbService;
    private final IScheduleService scheduleService;
    private final ISSEService sseService;

    /** RU: добавление урока + Server Sent Events
     * toDo: подправить на клиенте добавление уровня body
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addLesson")
    public ResponseEntity<Void> addLesson(@RequestBody ScheduleInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final Group group = dbService.groupById(body.group());
        if(group == null) return ResponseEntity.notFound().build();
        final Long schId = Long.parseLong(sub.getLvlSch()),
            teaId = body.obj().prepod.id;
        final User teaU = dbService.userById(teaId);
        final School school = dbService.schoolById(schId);

        final ScheduleOutDTO outDTO = scheduleService.addLesson(body, teaU, school, group);
        if(teaU != null) {
            sseService.sendEventFor("addLessonC", outDTO, TypesConnect.SCHEDULE, sub.getLvlSch(), "main", "tea", teaU.getId()+"");
        }
        sseService.sendEventFor("addLessonC", outDTO, TypesConnect.SCHEDULE, sub.getLvlSch(), "main", "ht", "main");
        sseService.sendEventFor("addLessonC", outDTO, TypesConnect.SCHEDULE, sub.getLvlSch(), group.getId()+"", "main", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: отправляет данные о расписании для группы
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getSchedule/{grId}")
    public ResponseEntity<ScheduleServiceDTO> getSchedule(@PathVariable Long grId, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        Group group = null;
        Long groupId = null;
        if(user.getSelRole() == Roles.KID) {
            group = user.getSelecRole().getGrp();
        } else if(user.getSelRole() == Roles.PARENT) {
            final User kidU = dbService.userById(user.getSelKid());
            if(kidU != null) {
                group = kidU.getRole(Roles.KID).getGrp();
            }
        } else if(user.getSelRole() == Roles.HTEACHER) {
            group = dbService.groupById(grId);
        }
        if(group != null) groupId = group.getId();

        final ScheduleServiceDTO outDTO = scheduleService.getShedule(user, groupId);
        sseService.changeSubscriber(auth.getUUID(), null, null, null, groupId+"", null, null);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] подтверждает клиенту права
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        final School school = dbService.getFirstRole(user.getRoles()).getYO();

        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.SCHEDULE, school.getId() +"", "main", "main", "main");
        return ResponseEntity.ok().build();
    }

    /** RU: [start] отправляет список групп и учителей учебного центра
     * toDo: подправить на клиенте добавление уровня Body
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('HTEACHER') OR hasAuthority('TEACHER'))""")
    @GetMapping("/getInfoToHT")
    public ResponseEntity<ScheduleOutDTO> getInfoForHTeacherOrTEACHER(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        String role = "main", teacherId = "main";
        if(user.getSelRole() == Roles.HTEACHER) role = "ht";
        if(user.getSelRole() == Roles.TEACHER) {
            role = "tea";
            teacherId = user.getId()+"";
        }

        final ScheduleOutDTO outDTO = scheduleService.prepareInfoForHTeacherOrTEACHER(user, school);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.SCHEDULE, school.getId() +"", "main", role, teacherId);
        return ResponseEntity.ok(outDTO);
    }

}