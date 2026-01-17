package ru.controllers.school.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.analytics.kidJournal.KidJournalOutDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.school.analytics.IKidJournalService;

import java.util.List;

/** RU: Контроллер для раздела просмотра оценок + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/KidJournalController">http://localhost:9001/swagger/htmlSwag/#/KidJournalController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/journal")
@RequiredArgsConstructor
@RestController public class KidJournalController {
    private final IDBService dbService;
    private final IKidJournalService kidJournalService;
    private final ISSEService sseService;

    /** RU: отправляет данные о итоговых оценках
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfoPers/")
    public ResponseEntity<KidJournalOutDTO> getInfoPers(@AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final Role firstRole = dbService.getFirstRole(user.getRoles());
        final School sch = firstRole.getYO();
        final Group group = firstRole.getGrp();
        final List<Period> periods = sch.getPeriods();
        if (group == null || ObjectUtils.isEmpty(periods)) {
            return ResponseEntity.notFound().build();
        }

        final KidJournalOutDTO outDTO = kidJournalService.preparePeriodMarks(periods, user, sch, group);
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] отправляет данные о оценках
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo/")
    public ResponseEntity<KidJournalOutDTO> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        final Role firstRole = dbService.getFirstRole(user.getRoles());
        final School sch = firstRole.getYO();
        final Group group = firstRole.getGrp();
        if(group == null) return ResponseEntity.notFound().build();

        final KidJournalOutDTO outDTO = kidJournalService.prepareInfo(sch, group);
        if(outDTO == null) return ResponseEntity.notFound().build();

        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.JOURNAL, sch.getId() +"", "main", "main", "main");
        return ResponseEntity.ok(outDTO);
    }
}