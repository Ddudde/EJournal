package ru.controllers.school.analytics;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.controllers.AuthController;
import ru.controllers.DocsHelpController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.User;
import ru.data.models.school.Group;
import ru.data.models.school.Mark;
import ru.data.models.school.Period;
import ru.data.models.school.School;
import ru.security.user.CustomToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static ru.Main.datas;

/** RU: Контроллер для раздела просмотра оценок + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/JournalController">http://localhost:9001/swagger/htmlSwag/#/JournalController</a>
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
@RequestMapping("/journal")
@RequiredArgsConstructor
@RestController public class JournalController {
    private final AuthController authController;

    /** RU: отправляет данные о итоговых оценках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfoPers")
    public ResponseEntity<JsonObject> getInfoPers(@AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoPers");
        final School sch = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        final Group group = datas.getDbService().getFirstRole(user.getRoles()).getGrp();
        final List<Period> periods = sch.getPeriods();
        if (group == null || ObjectUtils.isEmpty(periods)) {
            return ResponseEntity.notFound().build();
        }
        wrtr.name("bodyPers").beginObject();
        for (Period p : periods) {
            wrtr.name(p.getId()+"").value(p.getName());
        }
        wrtr.endObject();
        final List<Object[]> marks = datas.getDbService().getMarkRepository().uniqNameSubjectAndMarksByParams(user.getId(), "per", periods);
        final Map<String, List<Mark>> mapM = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collector.of(
                ArrayList<Mark>::new,
                (list, item) -> list.add((Mark) item[1]),
                (left, right) -> right
            )));
        System.out.println(mapM);
        if(!ObjectUtils.isEmpty(mapM)) {
            final List<String> lessonsByKid = datas.getDbService().getLessonRepository().uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
            for(String les : lessonsByKid) {
                if(mapM.containsKey(les)) continue;
                mapM.put(les, null);
            }
            wrtr.name("bodyM").beginObject();
            for (String les : mapM.keySet()) {
                wrtr.name(les).beginObject();
                for (Mark marksM : mapM.get(les)) {
                    wrtr.name(marksM.getPeriod().getId()+"").value(marksM.getMark());
                }
                wrtr.endObject();
            }
            wrtr.endObject();
        }
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправляет данные о оценках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfo");
        final School sch = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        final Group group = datas.getDbService().getFirstRole(user.getRoles()).getGrp();
        if(group == null) return ResponseEntity.notFound().build();

        final Period actPeriod = datas.getActualPeriodBySchool(sch);
        final List<Object[]> marks = datas.getDbService().getDayRepository().uniqNameSubjectAndDatAndMarksByParams(sch.getId(), group.getId(), actPeriod.getId());
        final Map<String, Map<String, List<Mark>>> mapD = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.groupingBy(
                obj1 -> (String) obj1[1],
                Collector.of(
                    ArrayList<Mark>::new,
                    (list, item) -> list.add((Mark) item[2]),
                    (left, right) -> right
                ))));
        System.out.println("mapD " + mapD);
        final List<String> lessonsByKid = datas.getDbService().getLessonRepository().uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
        for(String les : lessonsByKid) {
            if(mapD.containsKey(les)) continue;
            mapD.put(les, null);
        }
        getJournal(wrtr, mapD);
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.JOURNAL, sch.getId() +"", "main", "main", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Заполняет JSON.
     * Заполняет оценки с учётом возможности нескольких оценок за день.
     * <pre>
     * bodyJ : {
     *     "nameSubject" : {
     *         "days" : {
     *             "dateOfEvent" : {
     *                 intNumEvent : {
     *                     "stringMark",
     *                     weight,
     *                     "type"
     *                 }
     *             }
     *         }
     *     }
     * }
     * </pre>
     * @param wrtr json для заполнения и отправки
     * @param mapD Map с оценками. По названию дисциплины и дате
     * @throws IOException Исключение вызывается при ошибках с Json */
    private void getJournal(JsonTreeWriter wrtr, Map<String, Map<String, List<Mark>>> mapD) throws IOException {
        wrtr.name("bodyJ").beginObject();
        for (String nameSub : mapD.keySet()) {
            final Map<String, List<Mark>> mapM = mapD.get(nameSub);
            wrtr.name(nameSub).beginObject()
                .name("days").beginObject();
            if(!ObjectUtils.isEmpty(mapM)) {
                for (String dat : mapM.keySet()) {
                    int i1 = -1;
                    for (Mark marksM : mapM.get(dat)) {
                        wrtr.name(i1 == -1 ? dat : (dat + "," + i1)).beginObject()
                            .name("mark").value(marksM.getMark())
                            .name("weight").value(marksM.getWeight())
                            .name("type").value(marksM.getStyle())
                            .endObject();
                        i1++;
                    }
                }
            }
            wrtr.endObject().endObject();
        }
        wrtr.endObject();
    }
}