package ru.controllers.school.analytics;

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
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.school.PeriodRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;

/** RU: Контроллер для просмотра и редактирования периодов обучения учебного центра
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/PeriodsController">http://localhost:9001/swagger/htmlSwag/#/PeriodsController</a>
 * </pre> */
@Slf4j
@RequestMapping("/periods")
@RequiredArgsConstructor
@RestController public class PeriodsController {
    private final PeriodRepository periodRepository;
    private final SchoolRepository schoolRepository;
    private final MainService mainService;
    private final DBService dbService;

    /** RU: создаёт новый период обучения учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPer")
    public ResponseEntity<Void> addPer(@RequestBody DataPeriods body, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init(body.toString(), "[POST] /addPer");
        final School school = user.getSelecRole().getYO();
        final Period period = new Period();
        period.setName(body.name);
        period.setDateN(body.perN);
        period.setDateK(body.perK);
        periodRepository.saveAndFlush(period);
        school.getPeriods().add(period);
        schoolRepository.saveAndFlush(school);
        wrtr.name("id").value(period.getId())
            .name("body").beginObject()
            .name("name").value(period.getName())
            .name("perN").value(period.getDateN())
            .name("perK").value(period.getDateK())
            .endObject();
        return mainService.getObjR(ans -> {
            SSEController.sendEventFor("addPerC", ans, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: [start] отправляет данные о расписании периодов обучения учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(CustomToken auth, @AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfo");
        final School school = user.getSelecRole().getYO();
        if (ObjectUtils.isEmpty(school.getPeriods())) {
            return ResponseEntity.notFound().build();
        }
        log.trace(school.getPeriods() + "");
        wrtr.name("bodyP").beginObject();
        int i = 0;
        for (Period period : school.getPeriods()) {
            if(period == null) continue;
            wrtr.name(i+"").beginObject()
                .name("name").value(period.getName())
                .name("perN").value(period.getDateN())
                .name("perK").value(period.getDateK())
                .endObject();
            i++;
        }
        wrtr.endObject();
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые PeriodsController в методах
     * @see PeriodsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataPeriods {
        public final String name, perN, perK;
    }
}