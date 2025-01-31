package ru.controllers.school.analytics;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.controllers.DocsHelpController;
import ru.controllers.SSEController;
import ru.controllers.TypesConnect;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Mark;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.school.DayRepository;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.MarkRepository;
import ru.security.user.CustomToken;
import ru.services.MainService;
import ru.services.db.DBService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** RU: Контроллер для раздела просмотра оценок + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/KidJournalController">http://localhost:9001/swagger/htmlSwag/#/KidJournalController</a>
 * </pre>
 * @see SubscriberDTO */
@Slf4j
@RequestMapping("/journal")
@RequiredArgsConstructor
@RestController public class KidJournalController {
    private final MarkRepository markRepository;
    private final LessonRepository lessonRepository;
    private final DBService dbService;
    private final MainService mainService;
    private final DayRepository dayRepository;

    /** RU: отправляет данные о итоговых оценках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfoPers")
    public ResponseEntity<JsonObject> getInfoPers(@AuthenticationPrincipal SubscriberDTO sub) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfoPers");
        final School sch = dbService.getFirstRole(user.getRoles()).getYO();
        final Group group = dbService.getFirstRole(user.getRoles()).getGrp();
        final List<Period> periods = sch.getPeriods();
        if (group == null || ObjectUtils.isEmpty(periods)) {
            return ResponseEntity.notFound().build();
        }
        wrtr.name("bodyPers").beginObject();
        for (Period p : periods) {
            wrtr.name(p.getId()+"").value(p.getName());
        }
        wrtr.endObject();
        final List<Object[]> marks = markRepository
            .uniqNameSubjectAndMarksByParams(user.getId(), "per", periods);
        final Map<String, List<Mark>> marksByNameSubjects = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collector.of(
                ArrayList<Mark>::new,
                (list, item) -> list.add((Mark) item[1]),
                (left, right) -> right
            )));
        log.trace(marksByNameSubjects + "");
        if(!ObjectUtils.isEmpty(marksByNameSubjects)) {
            final List<String> lessonsByKid = lessonRepository
                .uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
            for(String les : lessonsByKid) {
                if(marksByNameSubjects.containsKey(les)) continue;
                marksByNameSubjects.put(les, null);
            }
            wrtr.name("bodyM").beginObject();
            for (Map.Entry<String, List<Mark>> entry : marksByNameSubjects.entrySet()) {
                final String les = entry.getKey();
                wrtr.name(les).beginObject();
                for (Mark marksM : entry.getValue()) {
                    wrtr.name(marksM.getPeriod().getId()+"").value(marksM.getMark());
                }
                wrtr.endObject();
            }
            wrtr.endObject();
        }
        return mainService.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправляет данные о оценках
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(@dbService.existUserBySubscription(#sub))
        and (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(@AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) throws Exception {
        final User user = dbService.userById(sub.getUserId());
        final JsonTreeWriter wrtr = mainService.init("", "[GET] /getInfo");
        final School sch = dbService.getFirstRole(user.getRoles()).getYO();
        final Group group = dbService.getFirstRole(user.getRoles()).getGrp();
        if(group == null) return ResponseEntity.notFound().build();

        final Period actPeriod = mainService.getActualPeriodBySchool(sch);
        final List<Object[]> marks = dayRepository
            .uniqNameSubjectAndDatAndMarksByParams(sch.getId(), group.getId(), actPeriod.getId());
        final Map<String, Map<String, List<Mark>>> mapD = marks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.groupingBy(
                obj1 -> (String) obj1[1],
                Collector.of(
                    ArrayList<Mark>::new,
                    (list, item) -> list.add((Mark) item[2]),
                    (left, right) -> right
                ))));
        log.trace("mapD " + mapD);
        if(mapD == null) return ResponseEntity.notFound().build();

        final List<String> lessonsByKid = lessonRepository
            .uniqSubNameBySchoolAndGrp(sch.getId(), group.getId());
        for(String les : lessonsByKid) {
            if(mapD.containsKey(les)) continue;
            mapD.put(les, null);
        }
        getJournal(wrtr, mapD);
        return mainService.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.JOURNAL, sch.getId() +"", "main", "main", "main");
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
                for (Map.Entry<String, List<Mark>> entry : mapM.entrySet()) {
                    int i1 = -1;
                    final String dat = entry.getKey();
                    for (Mark marksM : entry.getValue()) {
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