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
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.school.day.Day;
import ru.mirea.data.models.school.day.Mark;
import ru.mirea.services.ServerService;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequestMapping("/pjournal")
@NoArgsConstructor
@RestController public class PJournalController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addMark")
    public JsonObject addMark(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                ref.group = datas.groupById(body.group);
                User objU = datas.userById(body.kid);
                if (ref.group != null && user.getSelRole() == 2L && objU != null) {
                    School school = user.getRoles().get(2L).getYO();
                    ref.schId = school.getId();
                    List<Object[]> marks = datas.getDayRepository().uniqDatAndMarksByParams(ref.schId, user.getId(), ref.group.getId(), subscriber.getLvlMore2());
                    List<Day> days = datas.getDayRepository().findBySchoolAndTeacherAndGrpAndNameSubject(ref.schId, user.getId(), ref.group.getId(), subscriber.getLvlMore2());
                    Mark mark = null;
                    Day day = null;
                    boolean oldMark = false;
                    String[] lesDay = body.day.split(",");
                    if(!ObjectUtils.isEmpty(marks)) {
                        int les = 0;
                        if(lesDay.length > 1) les = Integer.parseInt(lesDay[1]) + 1;
                        Map<String, List<Long>> mapM = marks.stream().collect(Collectors.groupingBy(
                            obj -> (String) obj[0],
                            Collector.of(
                                ArrayList<Long>::new,
                                (list, item) -> list.addAll((List<Long>) item[1]),
                                (left, right) -> {left.addAll(right);return left;}
                            )));
                        System.out.println(mapM);
                        System.out.println(body.day + " " + Arrays.toString(lesDay));
                        if(mapM.containsKey(lesDay[0])) {
                            List<Mark> marksU = datas.getMarkRepository().findByIdInAndUsr(mapM.get(lesDay[0]), objU.getId());
                            System.out.println(marksU);
                            if (!ObjectUtils.isEmpty(marksU)) {
                                mark = marksU.get(les);
                                day = days.get(les);
                                oldMark = true;
                            }
                        }
                    }
                    if(day == null) {
                        day = new Day();
                        day.setDat(lesDay[0]);
                        day.setGrp(ref.group);
                        day.setTeacher(user);
                        day.setSchool(school);
                        day.setNameSubject(subscriber.getLvlMore2());
                        datas.getDayRepository().saveAndFlush(day);
                    }
                    if(mark == null) mark = new Mark();
                    mark.setMark(body.mark);
                    mark.setUsr(objU);
                    mark.setType("norm");
                    mark.setWeight(body.weight);
                    mark.setStyle(body.style);
                    datas.getMarkRepository().saveAndFlush(mark);
                    if(!oldMark) {
                        day.getMarks().add(mark);
                        datas.getDayRepository().saveAndFlush(day);
                    }
                    body.wrtr.name("kid").value(objU.getId())
                        .name("day").value(body.day)
                        .name("body").beginObject()
                        .name("mark").value(mark.getMark())
                        .name("weight").value(mark.getWeight())
                        .name("type").value(mark.getStyle())
                        .endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addMarkC", ans, TypesConnect.PJOURNAL, ref.schId +"", "main", "main", subscriber.getLvlMore2());
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfoP3")
    public JsonObject getInfoP3(@RequestBody DataPJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                Long schId = datas.getFirstRole(user.getRoles()).getYO().getId();
                Group group = datas.groupById(body.group);
                if (group != null) {
                    List<Object[]> marks = datas.getDayRepository().uniqDatAndMarksByParams(schId, user.getId(), group.getId(), subscriber.getLvlMore2());
                    Map<String, List<Long>> mapM = marks.stream().collect(Collectors.groupingBy(
                        obj -> (String) obj[0],
                        Collector.of(
                            ArrayList<Long>::new,
                            (list, item) -> list.addAll((List<Long>) item[1]),
                            (left, right) -> { left.addAll(right); return left;}
                        )));
                    System.out.println(mapM);
                    body.wrtr.name("bodyK").beginObject();
                    if (!ObjectUtils.isEmpty(group.getKids())) {
                        for (User objU : group.getKids()) {
                            if (objU == null) continue;
                            body.wrtr.name(objU.getId() + "").beginObject()
                                .name("name").value(objU.getFio())
                                .name("days").beginObject();
                            for (String dat : mapM.keySet()) {
                                List<Mark> marksU = datas.getMarkRepository().findByIdInAndUsr(mapM.get(dat), objU.getId());
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
                            List<Mark> marksU = datas.getMarkRepository().findByUsrAndWeight(objU.getId(), 0);
                            System.out.println("perU " + marksU);
                            for (Mark marksM : marksU) {
                                if(Objects.equals(marksM.getType(), "avg")) {
                                    body.wrtr.name("mark").value(marksM.getAvg());
                                }
                            }
                            body.wrtr.endObject().endObject();
                        }
                    }
                    if (!ObjectUtils.isEmpty(group.getKidsInv())) {
                        for (Invite objI : group.getKidsInv()) {
                            if (objI == null) continue;
                            body.wrtr.name(objI.getId() + "").beginObject()
                                .name("name").value(objI.getFio())
                                .name("days").beginObject();
                            for (String dat : mapM.keySet()) {
                                List<Mark> marksI = datas.getMarkRepository().findByIdInAndUserInv(mapM.get(dat), objI.getId());
                                int i1 = -1;
                                for (Mark marksM : marksI) {
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
                            List<Mark> marksI = datas.getMarkRepository().findByUserInvAndWeight(objI.getId(), 0);
                            System.out.println("perI " + marksI);
                            for (Mark marksM : marksI) {
                                if(Objects.equals(marksM.getType(), "avg")) {
                                    body.wrtr.name("mark").value(marksM.getAvg());
                                }
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
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                ref.schId = datas.getFirstRole(user.getRoles()).getYO().getId();
                List<Long> groupsL = datas.getLessonRepository().uniqGroupsBySchoolAndSubName(ref.schId, body.predm);
                if (!ObjectUtils.isEmpty(groupsL)){
                    System.out.println(groupsL);
                    body.wrtr.name("bodyG").beginObject();
                    for (Long i : groupsL) {
                        Group gr = datas.groupById(i);
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
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(2L)) {
                ref.schId = datas.getFirstRole(user.getRoles()).getYO().getId();
                List<String> subjs = datas.getLessonRepository().uniqSubNameBySchool(ref.schId);
                if (!ObjectUtils.isEmpty(subjs)){
                    System.out.println(subjs);
                    body.wrtr.name("bodyP").beginObject();
                    int i = 0;
                    for (String name : subjs) {
                        body.wrtr.name(i+"").value(name);
                        i++;
                    }
                    body.wrtr.endObject();
                }
                datas.getShedule("bodyS", user, body.wrtr, null);
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.PJOURNAL, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataPJournal {
    public String uuid, predm, style, day;
    public Long group, kid;
    public int weight, mark;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}