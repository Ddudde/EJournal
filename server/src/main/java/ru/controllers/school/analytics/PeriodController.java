package ru.controllers.school.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.DocsHelpController;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodOutDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodsInnerDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.ISSEService;
import ru.services.interfaces.logic.school.analytics.IPeriodService;

import java.util.List;

/** RU: Контроллер для просмотра и редактирования периодов обучения учебного центра
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/PeriodsController">http://localhost:9001/swagger/htmlSwag/#/PeriodsController</a>
 * </pre> */
@Slf4j
@RequestMapping("/periods")
@RequiredArgsConstructor
@RestController public class PeriodController {
    private final IDBService dbService;
    private final IPeriodService periodService;
    private final ISSEService sseService;

    /** RU: создаёт новый период обучения учебного центра
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPer")
    public ResponseEntity<Void> addPer(@RequestBody PeriodsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();

        final PeriodOutDTO outDTO = periodService.addPeriod(body, school);
        sseService.sendEventFor("addPerC", outDTO, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** RU: [start] отправляет данные о расписании периодов обучения учебного центра
     * @see DocsHelpController#point Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfo")
    public ResponseEntity<PeriodOutDTO> getInfo(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        final School school = user.getSelecRole().getYO();
        final List<Period> periods = school.getPeriods();
        if (ObjectUtils.isEmpty(periods)) return ResponseEntity.notFound().build();

        final PeriodOutDTO outDTO = periodService.prepareInfo(periods);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
        return ResponseEntity.ok(outDTO);
    }

}