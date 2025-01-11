package ru.controllers.school;

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
import ru.security.user.Roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static ru.Main.datas;

/** RU: Контроллер для просмотра дневника ученика.
 * Оценки могут быть на нескольких уроках одной дисциплины, а домашние задания выдаются на целый день.
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/DnevnikController">http://localhost:9001/swagger/htmlSwag/#/DnevnikController</a>
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
@RequestMapping("/dnevnik")
@RequiredArgsConstructor
@RestController public class DnevnikController {
    private final AuthController authController;

    /** RU: отправляет данные о расписании, оценках, домашних заданиях
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @GetMapping("/getDnevnik")
    public ResponseEntity<JsonObject> getDnevnik(@AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getDnevnik");
        Group group = null;
        if(user.getSelRole() == Roles.KID) {
            group = user.getRoles().get(Roles.KID).getGrp();
        } else if(user.getSelRole() == Roles.PARENT) {
            final User kidU = datas.getDbService().userById(user.getSelKid());
            if(kidU != null) {
                group = kidU.getRoles().get(Roles.KID).getGrp();
            }
        }
        if (group == null) return ResponseEntity.notFound().build();

        final School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
        datas.getShedule("body", user, wrtr, group.getId());

        final Period actPeriod = datas.getActualPeriodBySchool(school);
        final List<Object[]> marks = datas.getDbService().getDayRepository()
            .uniqNameSubjectAndDatAndMarksByParams(school.getId(), group.getId(), actPeriod.getId());
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
        wrtr.name("min").value(actPeriod.getDateN());
        wrtr.name("max").value(actPeriod.getDateK());
        final List<Object[]> homeworks = datas.getDbService().getDayRepository()
            .uniqNameSubAndDatAndHomeworkByParams(school.getId(), group.getId());
        final Map<String, Map<String, String>> mapH = homeworks.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.toMap(obj -> (String) obj[1], obj -> (String) obj[2])
        ));

        getJournal(wrtr, mapD, mapH);
        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: Заполняет JSON.
     * Если оценки существуют, то заполняет (оценки + домашние задания), в противном случае просто домашние задания.
     * <pre>
     * bodyD : {
     *     "nameSubject" : {
     *         "dateOfEvent" : {
     *             intNumEvent : {
     *                 "stringMark",
     *                 weight,
     *                 "type",
     *                 "homework"
     *             },
     *             i: 0
     *         }
     *     }
     * }
     * </pre>
     * @param wrtr json для заполнения и отправки
     * @param mapD Map с оценками. По названию дисциплины и дате
     * @param mapH Map с домашними заданиями. По названию дисциплины и дате
     * @throws IOException Исключение вызывается при ошибках с Json */
    private void getJournal(JsonTreeWriter wrtr, Map<String, Map<String, List<Mark>>> mapD, Map<String, Map<String, String>> mapH) throws IOException {
        wrtr.name("bodyD").beginObject();
        if(!ObjectUtils.isEmpty(mapD)) {
            for (String nameSub : mapD.keySet()) {
                final Map<String, List<Mark>> mapM = mapD.get(nameSub);
                wrtr.name(nameSub).beginObject();
                if (!ObjectUtils.isEmpty(mapM)) {
                    for (String dat : mapM.keySet()) {
                        wrtr.name(dat).beginObject();
                        int i1 = 0;
                        for (Mark marksM : mapM.get(dat)) {
                            wrtr.name(i1++ + "").beginObject()
                                .name("mark").value(marksM.getMark())
                                .name("weight").value(marksM.getWeight())
                                .name("type").value(marksM.getStyle());
                            if (i1 == 1 && !ObjectUtils.isEmpty(mapH) && mapH.containsKey(nameSub) && mapH.get(nameSub).containsKey(dat)) {
                                wrtr.name("homework").value(mapH.get(nameSub).get(dat));
                            }
                            wrtr.endObject();
                        }
                        wrtr.name("i").value(0)
                            .endObject();
                    }
                }
                wrtr.endObject();
            }
        } else if(!ObjectUtils.isEmpty(mapH)) {
            for (String nameSub : mapH.keySet()) {
                wrtr.name(nameSub).beginObject();
                if (!ObjectUtils.isEmpty(mapH.get(nameSub))) {
                    for (String dat : mapH.get(nameSub).keySet()) {
                        wrtr.name(dat).beginObject()
                            .name("0").beginObject();
                        if (mapH.get(nameSub).containsKey(dat)) {
                            wrtr.name("homework").value(mapH.get(nameSub).get(dat));
                        }
                        wrtr.endObject()
                            .name("i").value(0)
                            .endObject();
                    }
                }
                wrtr.endObject();
            }
        }
        wrtr.endObject();
    }

    /** RU: [start] запускает клиента в раздел Дневник и подтверждает клиенту права
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        AND (hasAuthority('KID') OR hasAuthority('PARENT'))""")
    @GetMapping("/getInfo")
    public ResponseEntity<Void> getInfo(@AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final Long schId = user.getSelecRole().getYO().getId();
        authController.infCon(auth.getUUID(), null, TypesConnect.DNEVNIK, schId +"", "main", "main", "main");
        return ResponseEntity.ok().build();
    }
}