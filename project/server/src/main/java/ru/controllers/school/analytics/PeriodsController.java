package ru.controllers.school.analytics;

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
import ru.controllers.AuthController;
import ru.controllers.DocsHelpController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.User;
import ru.data.models.school.Period;
import ru.data.models.school.School;
import ru.security.user.CustomToken;

import static ru.Main.datas;

/** RU: Контроллер для просмотра и редактирования периодов обучения учебного центра
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/PeriodsController">http://localhost:9001/swagger/htmlSwag/#/PeriodsController</a>
 *
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 *
 * </pre> */
@RequestMapping("/periods")
@RequiredArgsConstructor
@RestController public class PeriodsController {
    private final AuthController authController;

    /** RU: создаёт новый период обучения учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @PostMapping("/addPer")
    public ResponseEntity<Void> addPer(@RequestBody DataPeriods body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addPer");
        final School school = user.getSelecRole().getYO();
        final Period period = new Period();
        period.setName(body.name);
        period.setDateN(body.perN);
        period.setDateK(body.perK);
        datas.getDbService().getPeriodRepository().saveAndFlush(period);
        school.getPeriods().add(period);
        datas.getDbService().getSchoolRepository().saveAndFlush(school);
        wrtr.name("id").value(period.getId())
            .name("body").beginObject()
            .name("name").value(period.getName())
            .name("perN").value(period.getDateN())
            .name("perK").value(period.getDateK())
            .endObject();
        return datas.getObjR(ans -> {
            authController.sendEventFor("addPerC", ans, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: [start] отправляет данные о расписании периодов обучения учебного центра
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('HTEACHER')""")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfo");
        final School school = user.getSelecRole().getYO();
        if (ObjectUtils.isEmpty(school.getPeriods())) {
            return ResponseEntity.notFound().build();
        }
        System.out.println(school.getPeriods());
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
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.PERIODS, school.getId() +"", "main", "main", "main");
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