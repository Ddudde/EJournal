package ru.controllers.school;

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
import ru.data.DAO.auth.User;
import ru.data.DAO.school.*;
import ru.data.DTO.PrepareMarkDTO;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static ru.Main.datas;

/** RU: Контроллер для просмотра и редактирования журнала(оценки и домашние задания) группы
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/TeacherJournalController">http://localhost:9001/swagger/htmlSwag/#/TeacherJournalController</a>
 * </pre> */
@Slf4j
@RequestMapping("/pjournal")
@RequiredArgsConstructor
@RestController public class TeacherJournalController {

    /** RU: создаёт домашнее задание на определённое занятие дня группе
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('TEACHER')""")
    @PostMapping("/addHomework")
    public ResponseEntity<Void> addHomework(@RequestBody DataPJournal body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addHomework");
        final Group group = datas.getDbService().groupById(body.group);
        if (group == null) return ResponseEntity.notFound().build();

        final School school = user.getSelecRole().getYO();
        final String[] lesDay = body.day.split(",");
        Day day = datas.getDbService().getDayRepository()
            .findBySchoolIdAndTeacherIdAndGrpIdAndNameSubjectAndDat(school.getId(), user.getId(), group.getId(), sub.getLvlMore2(), lesDay[0]);
        if (day == null) {
            day = new Day();
            day.setDat(lesDay[0]);
            day.setGrp(group);
            day.setTeacher(user);
            day.setSchool(school);
            day.setNameSubject(sub.getLvlMore2());
        }
        day.setHomework(body.homework);
        datas.getDbService().getDayRepository().saveAndFlush(day);
        wrtr.name("day").value(body.day)
            .name("homework").value(body.homework);
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("addHomeworkC", ans, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", sub.getLvlMore2());
        }, wrtr, HttpStatus.CREATED);
    }

    /** RU: создаёт оценку к определённому уроку либо целому периоду(итоговая оценка)
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('TEACHER')""")
    @PostMapping("/addMark")
    public ResponseEntity<Void> addMark(@RequestBody DataPJournal body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addMark");
        final Group group = datas.getDbService().groupById(body.group);
        final User objU = datas.getDbService().userById(body.kid);
        if (group == null || objU == null) {
            return ResponseEntity.notFound().build();
        }
        final School school = user.getSelecRole().getYO();
        final boolean isNotPeriodMark = body.per == null;
        final PrepareMarkDTO prepareMarkDTO = prepareMarkForCreate(school, user, body, group, sub.getLvlMore2());
        Mark mark = prepareMarkDTO.mark;
        if(prepareMarkDTO.mark == null) mark = new Mark();
        mark.setMark(body.mark);
        mark.setUsr(objU);
        mark.setPeriod(prepareMarkDTO.period);
        mark.setType(isNotPeriodMark ? "norm" : "per");
        mark.setWeight(body.weight);
        mark.setStyle(isNotPeriodMark ? body.style : sub.getLvlMore2());
        datas.getDbService().getMarkRepository().saveAndFlush(mark);
        if(isNotPeriodMark && !prepareMarkDTO.oldMark) {
            prepareMarkDTO.day.getMarks().add(mark);
            datas.getDbService().getDayRepository().saveAndFlush(prepareMarkDTO.day);
        }
        wrtr.name("kid").value(objU.getId())
            .name("day").value(body.day)
            .name("body").beginObject()
            .name("mark").value(mark.getMark())
            .name("weight").value(mark.getWeight())
            .name("type").value(mark.getStyle())
            .name("per").value(body.per)
            .endObject();
        return datas.getObjR(ans -> {
            SSEController.sendEventFor("addMarkC", ans, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", sub.getLvlMore2());
        }, wrtr, HttpStatus.CREATED);
    }

    /**
     * @return PrepareMarkDTO.oldMark.true - оценка существует */
    private PrepareMarkDTO prepareMarkForCreate(School school, User user, DataPJournal body, Group group, String nameSubject) {
        final boolean isNotPeriodMark = body.per == null;
        PrepareMarkDTO prepareMarkDTO;
        if(!isNotPeriodMark) {
            prepareMarkDTO = new PrepareMarkDTO();
            prepareMarkDTO.period = datas.getDbService().periodById(body.per);
            prepareMarkDTO.mark = datas.getDbService().getMarkRepository()
                .findByTypeAndStyleAndPeriodIdAndUsrId("per", nameSubject, body.per, body.kid);
            prepareMarkDTO.oldMark = false;
            return prepareMarkDTO;
        }
        //Видимо отправляется номер последней существующей оценки
        final String[] dayAndNumOfMark = body.day.split(",");
        prepareMarkDTO = getExistMark(body, school, user, group, nameSubject, dayAndNumOfMark);
        if (prepareMarkDTO.day != null) return prepareMarkDTO;

        final Day day = new Day();
        day.setDat(dayAndNumOfMark[0]);
        day.setGrp(group);
        day.setTeacher(user);
        day.setSchool(school);
        day.setNameSubject(nameSubject);
        datas.getDbService().getDayRepository().saveAndFlush(day);
        prepareMarkDTO.day = day;
        return prepareMarkDTO;
    }

    //toDo: перепроверить #numLes
    private PrepareMarkDTO getExistMark(DataPJournal body, School school, User user, Group group, String nameSubject, String[] dayAndNumOfMark) {
        final PrepareMarkDTO prepareMarkDTO = new PrepareMarkDTO();
        prepareMarkDTO.period = datas.getActualPeriodBySchool(school);
        final Map<String, List<Long>> marksIdByDay = getMarksByDay(school.getId(), user.getId(), group.getId(), nameSubject);
        if (ObjectUtils.isEmpty(marksIdByDay)) return prepareMarkDTO;

        final List<Day> days = datas.getDbService().getDayRepository()
            .findBySchoolIdAndTeacherIdAndGrpIdAndNameSubject(school.getId(), user.getId(), group.getId(), nameSubject);
        int numLes = 0;
        if (dayAndNumOfMark.length > 1) {
            numLes = Integer.parseInt(dayAndNumOfMark[1]) + 1;
        }
        if (!marksIdByDay.containsKey(dayAndNumOfMark[0])) return prepareMarkDTO;

        final List<Mark> marksOfKidAndDay = datas.getDbService().getMarkRepository()
            .findByIdInAndUsrId(marksIdByDay.get(dayAndNumOfMark[0]), body.kid);
        log.trace(marksOfKidAndDay + "");
        if (!ObjectUtils.isEmpty(marksOfKidAndDay)) {
            prepareMarkDTO.mark = marksOfKidAndDay.get(numLes);
            prepareMarkDTO.day = days.get(numLes);
            prepareMarkDTO.oldMark = true;
        }
        return prepareMarkDTO;
    }

    /** RU: отправляет данные о оценках, домашних заданиях и итоговых оценках группы подчинённой преподавателю на дисциплине
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP3/{groupId}")
    public ResponseEntity<JsonObject> getInfoPart3(@PathVariable Long groupId, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init(groupId + "", "[GET] /getInfoP3");
        final School school = user.getSelecRole().getYO();
        final Group group = datas.getDbService().groupById(groupId);
        if (group == null) return ResponseEntity.notFound().build();

        final Map<String, List<Long>> marksByDay = getMarksByDay(school.getId(), user.getId(), group.getId(), sub.getLvlMore2());
        log.trace(marksByDay + "");
        wrtr.name("bodyD").beginObject();
        final List<Object[]> homeworks = datas.getDbService().getDayRepository()
            .uniqDatAndHomeworkByParams(school.getId(), group.getId(), sub.getLvlMore2());
        final Map<String, String> homeworkByDay = homeworks.stream()
            .filter(obj->obj[0] != null && obj[1] != null)
            .collect(Collectors.toMap(s -> (String) s[0], s -> (String) s[1],
                (first, second) -> first));
        if (homeworkByDay != null) {
            for (String dat : homeworkByDay.keySet()) {
                wrtr.name(dat).value(homeworkByDay.get(dat));
            }
        }
        wrtr.endObject();
        getJournalAndPeriods(marksByDay, wrtr, group.getKids(), school, sub.getLvlMore2());

        return datas.getObjR(ans -> {}, wrtr, HttpStatus.OK, false);
    }

    /** RU: заполняет JSON.
     * Оценки группы данного периода и итоговые на дисциплине.
     * <pre>
     * bodyK : {
     *     idKid : {
     *         name : "FIO",
     *         days : {
     *             stringDate : {
     *                 "stringMark",
     *                 weight,
     *                 "type"
     *             }
     *         },
     *         avg : {
     *             idPeriod : "stringMark"
     *         }
     *     }
     * }
     * </pre>
     * @param wrtr json для заполнения и отправки
     * @param marksByDay Map с оценками. По дате
     * @throws IOException Исключение вызывается при ошибках с Json */
    private void getJournalAndPeriods(Map<String, List<Long>> marksByDay, JsonTreeWriter wrtr, List<User> kids, School school, String nameSubject) throws IOException {
        final Period actPeriod = datas.getActualPeriodBySchool(school);
        wrtr.name("bodyK").beginObject();
        if (ObjectUtils.isEmpty(kids) || ObjectUtils.isEmpty(marksByDay)) {
            wrtr.endObject();
            return;
        }
        for (User kid : kids) {
            if (kid == null) continue;

            wrtr.name(kid.getId() + "").beginObject()
                .name("name").value(kid.getFio())
                .name("days").beginObject();
            for (String dat : marksByDay.keySet()) {
                //Уточняет по отдельному ученику и периоду обучения из оценок всей группы
                final List<Mark> marksOfKid = datas.getDbService().getMarkRepository()
                    .findByIdInAndUsrIdAndPeriodId(marksByDay.get(dat), kid.getId(), actPeriod.getId());
                log.trace(dat);
                log.trace(marksOfKid + "");

                int i1 = -1;
                for (Mark marksM : marksOfKid) {
                    wrtr.name(i1 == -1 ? dat : (dat + "," + i1)).beginObject()
                        .name("mark").value(marksM.getMark())
                        .name("weight").value(marksM.getWeight())
                        .name("type").value(marksM.getStyle())
                        .endObject();
                    i1++;
                }
            }

            wrtr.endObject()
                .name("avg").beginObject();
            final List<Mark> periodMarksOfKid = datas.getDbService().getMarkRepository()
                .findByPeriodInAndTypeAndStyleAndUsrId(school.getPeriods(), "per", nameSubject, kid.getId());
            log.trace("perU " + periodMarksOfKid);
            for (Mark marksM : periodMarksOfKid) {
                wrtr.name(marksM.getPeriod().getId()+"").value(marksM.getMark());
            }
            wrtr.endObject().endObject();
        }
        wrtr.endObject();
    }

    /** RU: получает оценки всей группы в рамках дисциплины по дням */
    private Map<String, List<Long>> getMarksByDay(Long schoolId, Long userId, Long groupId, String nameSubject) {
        final List<Object[]> marksByDay = datas.getDbService().getDayRepository()
            .uniqDatAndMarksByParams(schoolId, userId, groupId, nameSubject);
        if (ObjectUtils.isEmpty(marksByDay)) return null;
        return marksByDay.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collector.of(
                ArrayList<Long>::new,
                (list, item) -> list.add((Long) item[1]),
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
            )));
    }

    /** RU: [start] отправляет данные о группах учебного центра подчинённые преподавателю на дисциплине
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP2/{nameSubject}")
    public ResponseEntity<JsonObject> getInfoPart2(CustomToken auth, @PathVariable String nameSubject, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoP2");
        final School school = user.getSelecRole().getYO();
        final List<Long> groupsL = datas.getDbService().getLessonRepository()
            .uniqGroupsBySchoolAndSubNameAndTeacher(school.getId(), nameSubject, user.getId());
        if (ObjectUtils.isEmpty(groupsL)) {
            return ResponseEntity.notFound().build();
        }
        log.trace(groupsL + "");
        wrtr.name("bodyG").beginObject();
        for (Long i : groupsL) {
            final Group gr = datas.getDbService().groupById(i);
            wrtr.name(i + "").value(gr.getName());
        }
        wrtr.endObject()
            .name("firstG").value(groupsL.get(0));
        return datas.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.PJOURNAL, null, null, null, nameSubject);
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: [start] отправляет данные о расписании, периодах обучения и дисциплинах преподавателя
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("""
        @code401.check(#sub.getUser() != null)
        and hasAuthority('TEACHER')""")
    @GetMapping("/getInfoP1")
    public ResponseEntity<JsonObject> getInfoPart1(CustomToken auth, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("", "[GET] /getInfoP1");
        final School school = user.getSelecRole().getYO();
        final List<String> subjs = datas.getDbService().getLessonRepository()
            .uniqSubNameBySchoolAndTeacher(school.getId(), user.getId());
        if (!ObjectUtils.isEmpty(subjs)){
            log.trace(String.valueOf(subjs));
            wrtr.name("bodyPred").beginObject();
            int i = 0;
            for (String name : subjs) {
                wrtr.name(i+"").value(name);
                i++;
            }
            wrtr.endObject();
        }
        final List<Period> periods = school.getPeriods();
        if (!ObjectUtils.isEmpty(periods)){
            wrtr.name("bodyPers").beginObject();
            for (Period p : periods) {
                wrtr.name(p.getId()+"").value(p.getName());
            }
            wrtr.endObject();
        }
        final Period actPeriod = datas.getActualPeriodBySchool(school);
        wrtr.name("min").value(actPeriod.getDateN());
        wrtr.name("max").value(actPeriod.getDateK());
        datas.getShedule("bodyS", user, wrtr, null);
        return datas.getObjR(ans -> {
            SSEController.changeSubscriber(auth.getUUID(), null, TypesConnect.PJOURNAL, school.getId() +"", "main", "main", "main");
        }, wrtr, HttpStatus.OK, false);
    }

    /** RU: Данные клиента используемые PJournalController в методах
     * @see TeacherJournalController */
    @ToString
    @RequiredArgsConstructor
    static final class DataPJournal {
        public final String style, day, mark, homework;
        public final Long group, kid, per;
        public final int weight;
    }
}