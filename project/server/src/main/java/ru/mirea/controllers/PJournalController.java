package ru.mirea.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.Main;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.services.MainService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequestMapping("/pjournal")
@NoArgsConstructor
@RestController public class PJournalController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addHomework")
    public JsonObject addHomework(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                ref.group = datas.getDbService().groupById(body.group);
                if (ref.group != null && user.getSelRole() == 2L) {
                    School school = user.getRoles().get(2L).getYO();
                    ref.schId = school.getId();
                    String[] lesDay = body.day.split(",");
                    Day day = datas.getDbService().getDayRepository().findBySchoolIdAndTeacherIdAndGrpIdAndNameSubjectAndDat(ref.schId, user.getId(), ref.group.getId(), subscriber.getLvlMore2(), lesDay[0]);
                    if (day == null) {
                        day = new Day();
                        day.setDat(lesDay[0]);
                        day.setGrp(ref.group);
                        day.setTeacher(user);
                        day.setSchool(school);
                        day.setNameSubject(subscriber.getLvlMore2());
                    }
                    day.setHomework(body.homework);
                    datas.getDbService().getDayRepository().saveAndFlush(day);
                    body.wrtr.name("day").value(body.day)
                        .name("homework").value(body.homework);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("addHomeworkC", ans, TypesConnect.PJOURNAL, ref.schId +"", "main", "main", subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/addMark")
    public JsonObject addMark(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                ref.group = datas.getDbService().groupById(body.group);
                User objU = datas.getDbService().userById(body.kid);
                if (ref.group != null && user.getSelRole() == 2L && objU != null) {
                    School school = user.getRoles().get(2L).getYO();
                    ref.schId = school.getId();
                    Mark mark = null;
                    Day day = null;
                    boolean oldMark = false,
                        typ = body.per == null;
                    if(typ) {
                        String[] lesDay = body.day.split(",");
                        List<Object[]> marksByDay = datas.getDbService().getDayRepository().uniqDatAndMarksByParams(ref.schId, user.getId(), ref.group.getId(), subscriber.getLvlMore2());
                        List<Day> days = datas.getDbService().getDayRepository().findBySchoolIdAndTeacherIdAndGrpIdAndNameSubject(ref.schId, user.getId(), ref.group.getId(), subscriber.getLvlMore2());
                        if (!ObjectUtils.isEmpty(marksByDay)) {
                            int les = 0;
                            if (lesDay.length > 1) les = Integer.parseInt(lesDay[1]) + 1;
                            Map<String, List<Long>> mapM = marksByDay.stream().collect(Collectors.groupingBy(
                                obj -> (String) obj[0],
                                Collector.of(
                                    ArrayList<Long>::new,
                                    (list, item) -> list.add((Long) item[1]),
                                    (left, right) -> {
                                        left.addAll(right);
                                        return left;
                                    }
                                )));
                            if (mapM.containsKey(lesDay[0])) {
                                List<Mark> marksU = datas.getDbService().getMarkRepository().findByIdInAndUsrId(mapM.get(lesDay[0]), objU.getId());
                                System.out.println(marksU);
                                if (!ObjectUtils.isEmpty(marksU)) {
                                    mark = marksU.get(les);
                                    day = days.get(les);
                                    oldMark = true;
                                }
                            }
                        }
                        if (day == null) {
                            day = new Day();
                            day.setDat(lesDay[0]);
                            day.setGrp(ref.group);
                            day.setTeacher(user);
                            day.setSchool(school);
                            day.setNameSubject(subscriber.getLvlMore2());
                            datas.getDbService().getDayRepository().saveAndFlush(day);
                        }
                    } else {
                        mark = datas.getDbService().getMarkRepository().findByTypeAndStyleAndPeriodIdAndUsrId("per", subscriber.getLvlMore2(), body.per, objU.getId());
                    }
                    if(mark == null) mark = new Mark();
                    mark.setMark(body.mark);
                    mark.setUsr(objU);
                    mark.setPeriod(typ ? datas.getActualPeriodBySchool(school) : datas.getDbService().periodById(body.per));
                    mark.setType(typ ? "norm" : "per");
                    mark.setWeight(body.weight);
                    mark.setStyle(typ ? body.style : subscriber.getLvlMore2());
                    datas.getDbService().getMarkRepository().saveAndFlush(mark);
                    if(typ && !oldMark) {
                        day.getMarks().add(mark);
                        datas.getDbService().getDayRepository().saveAndFlush(day);
                    }
                    body.wrtr.name("kid").value(objU.getId())
                        .name("day").value(body.day)
                        .name("body").beginObject()
                        .name("mark").value(mark.getMark())
                        .name("weight").value(mark.getWeight())
                        .name("type").value(mark.getStyle())
                        .name("per").value(body.per)
                        .endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageFor("addMarkC", ans, TypesConnect.PJOURNAL, ref.schId +"", "main", "main", subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfoP3")
    public JsonObject getInfoP3(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                School school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                Group group = datas.getDbService().groupById(body.group);
                if (group != null) {
                    List<Object[]> marks = datas.getDbService().getDayRepository().uniqDatAndMarksByParams(school.getId(), user.getId(), group.getId(), subscriber.getLvlMore2());
                    Map<String, List<Long>> mapM = marks.stream().collect(Collectors.groupingBy(
                        obj -> (String) obj[0],
                        Collector.of(
                            ArrayList<Long>::new,
                            (list, item) -> list.add((Long) item[1]),
                            (left, right) -> right
                        )));
                    System.out.println(mapM);
                    Period actPeriod = datas.getActualPeriodBySchool(school);
                    body.wrtr.name("bodyD").beginObject();
                    List<Object[]> homeworks = datas.getDbService().getDayRepository().uniqDatAndHomeworkByParams(school.getId(), group.getId(), subscriber.getLvlMore2());
                    Map<String, String> mapD = homeworks.stream().collect(Collectors
                        .toMap(s -> (String) s[0], s -> (String) s[1]));
                    for (String dat : mapD.keySet()) {
                        body.wrtr.name(dat).value(mapD.get(dat));
                    }
                    body.wrtr.endObject();
                    body.wrtr.name("bodyK").beginObject();
//                    List<String> persS = datas.getDbService().getSchoolRepository().uniqPeriodsById(school.getId());
                    if (!ObjectUtils.isEmpty(group.getKids())) {
                        for (User objU : group.getKids()) {
                            if (objU == null) continue;
                            body.wrtr.name(objU.getId() + "").beginObject()
                                .name("name").value(objU.getFio())
                                .name("days").beginObject();
                            for (String dat : mapM.keySet()) {
                                List<Mark> marksU = datas.getDbService().getMarkRepository().findByIdInAndUsrIdAndPeriodId(mapM.get(dat), objU.getId(), actPeriod.getId());
                                System.out.println(dat);
                                System.out.println(marksU);

                                int i1 = -1;
                                for (Mark marksM : marksU) {
                                    body.wrtr.name(i1 == -1 ? dat : (dat + "," + i1)).beginObject()
                                        .name("mark").value(marksM.getMark())
                                        .name("weight").value(marksM.getWeight())
                                        .name("type").value(marksM.getStyle())
                                        .endObject();
                                    i1++;
                                }
                            }
                            body.wrtr.endObject()
                                .name("avg").beginObject();
                            List<Mark> marksU = datas.getDbService().getMarkRepository().findByPeriodInAndTypeAndStyleAndUsrId(school.getPeriods(), "per", subscriber.getLvlMore2(), objU.getId());
                            System.out.println("perU " + marksU);
                            for (Mark marksM : marksU) {
                                body.wrtr.name(marksM.getPeriod().getId()+"").value(marksM.getMark());
                            }
                            body.wrtr.endObject().endObject();
                        }
                    }
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {}, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfoP2")
    public JsonObject getInfoP2(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            School schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                ref.schId = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                List<Long> groupsL = datas.getDbService().getLessonRepository().uniqGroupsBySchoolAndSubNameAndTeacher(ref.schId.getId(), body.predm, user.getId());
                if (!ObjectUtils.isEmpty(groupsL)){
                    System.out.println(groupsL);
                    body.wrtr.name("bodyG").beginObject();
                    for (Long i : groupsL) {
                        Group gr = datas.getDbService().groupById(i);
                        body.wrtr.name(i + "").value(gr.getName());
                    }
                    body.wrtr.endObject()
                        .name("firstG").value(groupsL.get(0));
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.PJOURNAL, null, null, null, body.predm);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfoP1")
    public JsonObject getInfoP1(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            School school = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                ref.school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                List<String> subjs = datas.getDbService().getLessonRepository().uniqSubNameBySchoolAndTeacher(ref.school.getId(), user.getId());
                if (!ObjectUtils.isEmpty(subjs)){
                    System.out.println(subjs);
                    body.wrtr.name("bodyPred").beginObject();
                    int i = 0;
                    for (String name : subjs) {
                        body.wrtr.name(i+"").value(name);
                        i++;
                    }
                    body.wrtr.endObject();
                }
                List<Period> periods = ref.school.getPeriods();
                if (!ObjectUtils.isEmpty(periods)){
                    body.wrtr.name("bodyPers").beginObject();
                    for (Period p : periods) {
                        body.wrtr.name(p.getId()+"").value(p.getName());
                    }
                    body.wrtr.endObject();
                }
                Period actPeriod = datas.getActualPeriodBySchool(ref.school);
                body.wrtr.name("min").value(actPeriod.getDateN());
                body.wrtr.name("max").value(actPeriod.getDateK());
                datas.getShedule("bodyS", user, body.wrtr, null);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.PJOURNAL, ref.school.getId() +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataPJournal {
    public String uuid, predm, style, day, mark, homework;
    public Long group, kid, per;
    public int weight;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}