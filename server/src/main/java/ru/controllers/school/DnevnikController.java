package ru.controllers.school;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.dnevnik.DnevnikOutDTO;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.db.IDBService;
import ru.services.logic.SSE.ISSEService;
import ru.services.logic.school.IDnevnikService;

/** RU: Контроллер для просмотра дневника ученика.
 * Оценки могут быть на нескольких уроках одной дисциплины, а домашние задания выдаются на целый день.
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/DnevnikController">http://localhost:9001/swagger/htmlSwag/#/DnevnikController</a>
 * </pre> */
@Slf4j
@RequestMapping("/dnevnik")
@RequiredArgsConstructor
@RestController public class DnevnikController {
    private final IDBService dbService;
    private final IDnevnikService dnevnikService;
    private final ISSEService sseService;

    /** RU: отправляет данные о расписании, оценках, домашних заданиях
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @GetMapping("/getDnevnik")
    public ResponseEntity<DnevnikOutDTO> getDnevnik(@AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        Group group = null;
        if(user.getSelRole() == Roles.KID) {
            group = user.getRole(Roles.KID).getGrp();
        } else if(user.getSelRole() == Roles.PARENT) {
            final User kidU = dbService.userById(user.getSelKid());
            if(kidU != null) {
                group = kidU.getRole(Roles.KID).getGrp();
            }
        }
        if (group == null) return ResponseEntity.notFound().build();

        final DnevnikOutDTO outDTO = dnevnikService.prepareDnevnik(user, group.getId());
        return ResponseEntity.ok(outDTO);
    }

    /** RU: [start] запускает клиента в раздел Дневник и подтверждает клиенту права
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        AND (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> startDnevkik(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        final Long schId = user.getSelecRole().getYO().getId();
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.DNEVNIK, schId +"", "main", "main", "main");
        return ResponseEntity.ok().build();
    }
}