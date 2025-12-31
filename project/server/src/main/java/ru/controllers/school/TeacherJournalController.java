package ru.controllers.school;

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
import ru.data.DTO.controller.school.teacherJournal.TeacherJournalInnerDTO;
import ru.data.DTO.controller.school.teacherJournal.TeacherJournalOutDTO;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.security.user.CustomToken;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.school.ITeacherJournalService;

/** RU: Контроллер для просмотра и редактирования журнала(оценки и домашние задания) группы
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TeacherJournalController">http://localhost:9001/swagger/htmlSwag/#/TeacherJournalController</a>
 * </pre> */
@Slf4j
@RequestMapping("/pjournal")
@RequiredArgsConstructor
@RestController public class TeacherJournalController {
    private final IDBService dbService;
    private final ITeacherJournalService teacherJournalService;
    private final ISSEService sseService;

    /** RU: создаёт домашнее задание на определённое занятие дня группе
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('TEACHER')""")
    @PostMapping("/addHomework")
    public ResponseEntity<Void> addHomework(@RequestBody TeacherJournalInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final Group group = dbService.groupById(body.group);
        if (group == null) return ResponseEntity.notFound().build();
        final School school = user.getSelecRole().getYO();

        final TeacherJournalOutDTO outDTO = teacherJournalService.addHomework(body, sub, user, group, school);
        sseService.sendEventFor("addHomeworkC", outDTO, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", sub.getLvlMore2());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: создаёт оценку к определённому уроку либо целому периоду(итоговая оценка)
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('TEACHER')""")
    @PostMapping("/addMark")
    public ResponseEntity<Void> addMark(@RequestBody TeacherJournalInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final Group group = dbService.groupById(body.group);
        final User objU = dbService.userById(body.kid);
        if (group == null || objU == null) return ResponseEntity.notFound().build();
        final School school = user.getSelecRole().getYO();

        final TeacherJournalOutDTO outDTO = teacherJournalService.addMark(body, sub, school, user, group, objU);
        sseService.sendEventFor("addMarkC", outDTO, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", sub.getLvlMore2());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: отправляет данные о оценках, домашних заданиях и итоговых оценках группы подчинённой преподавателю на дисциплине
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP3/{groupId}")
    public ResponseEntity<TeacherJournalOutDTO> getInfoPart3(@PathVariable Long groupId, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();
        final Group group = dbService.groupById(groupId);
        if (group == null) return ResponseEntity.notFound().build();

        final TeacherJournalOutDTO outDTO = teacherJournalService.prepareMarksAndHomeworksForGroup(sub, school, user, group);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] отправляет данные о группах учебного центра подчинённые преподавателю на дисциплине
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP2/{nameSubject}")
    public ResponseEntity<GroupServiceDTO> getInfoPart2(CustomToken auth, @PathVariable String nameSubject, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();

        final GroupServiceDTO outDTO = teacherJournalService.groupsByList(school.getId(), nameSubject, user.getId());
        if (outDTO == null) return ResponseEntity.notFound().build();
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.PJOURNAL, null, null, null, nameSubject);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] отправляет данные о расписании, периодах обучения и дисциплинах преподавателя
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP1")
    public ResponseEntity<TeacherJournalOutDTO> getInfoPart1(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();

        final TeacherJournalOutDTO outDTO = teacherJournalService.prepareScheduleAndPeriods(school, user);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", "main");
        return ResponseEntity.ok(outDTO);
    }

}