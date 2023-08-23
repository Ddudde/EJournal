package ru.mirea.controllers.analytics;

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
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.Subscriber;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.services.ServerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequestMapping("/journal")
@NoArgsConstructor
@RestController public class JournalController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/getInfoPers")
    public JsonObject getInfoPers(@RequestBody DataJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            School sch = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                ref.sch = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                Group group = datas.getDbService().getFirstRole(user.getRoles()).getGrp();
                if(group != null && (user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L))) {
                    List<Period> periods = ref.sch.getPeriods();
                    if (!ObjectUtils.isEmpty(periods)){
                        body.wrtr.name("bodyPers").beginObject();
                        for (Period p : periods) {
                            body.wrtr.name(p.getId()+"").value(p.getName());
                        }
                        body.wrtr.endObject();
                        List<Object[]> marks = datas.getDbService().getMarkRepository().uniqNameSubjectAndMarksByParams(user.getId(), "per", periods);
                        Map<String, List<Mark>> mapM = marks.stream().collect(Collectors.groupingBy(
                            obj -> (String) obj[0],
                            Collector.of(
                                ArrayList<Mark>::new,
                                (list, item) -> list.add((Mark) item[1]),
                                (left, right) -> right
                            )));
                        System.out.println(mapM);
                        if(!ObjectUtils.isEmpty(mapM)) {
                            List<String> lessonsByKid = datas.getDbService().getLessonRepository().uniqSubNameBySchoolAndGrp(ref.sch.getId(), group.getId());
                            for(String les : lessonsByKid) {
                                if(mapM.containsKey(les)) continue;
                                mapM.put(les, null);
                            }
                            body.wrtr.name("bodyM").beginObject();
                            for (String les : mapM.keySet()) {
                                body.wrtr.name(les).beginObject();
                                for (Mark marksM : mapM.get(les)) {
                                    body.wrtr.name(marksM.getPeriod().getId()+"").value(marksM.getMark());
                                }
                                body.wrtr.endObject();
                            }
                            body.wrtr.endObject();
                        }
                    }
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.JOURNAL, ref.sch +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataJournal body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            School sch = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null) {
                ref.sch = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                Group group = datas.getDbService().getFirstRole(user.getRoles()).getGrp();
                if(group != null && (user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L))) {
                    Period actPeriod = datas.getActualPeriodBySchool(ref.sch);
                    List<Object[]> marks = datas.getDbService().getDayRepository().uniqNameSubjectAndDatAndMarksByParams(ref.sch.getId(), group.getId(), actPeriod.getId());
                    Map<String, Map<String, List<Mark>>> mapD = marks.stream().collect(Collectors.groupingBy(
                        obj -> (String) obj[0],
                        Collectors.groupingBy(
                            obj1 -> (String) obj1[1],
                            Collector.of(
                                ArrayList<Mark>::new,
                                (list, item) -> list.add((Mark) item[2]),
                                (left, right) -> right
                            ))));
                    System.out.println("mapD " + mapD);
                    List<String> lessonsByKid = datas.getDbService().getLessonRepository().uniqSubNameBySchoolAndGrp(ref.sch.getId(), group.getId());
                    for(String les : lessonsByKid) {
                        if(mapD.containsKey(les)) continue;
                        mapD.put(les, null);
                    }
                    body.wrtr.name("bodyJ").beginObject();
                    for (String nameSub : mapD.keySet()) {
                        Map<String, List<Mark>> mapM = mapD.get(nameSub);
                        body.wrtr.name(nameSub).beginObject()
                            .name("days").beginObject();
                        if(!ObjectUtils.isEmpty(mapM)) {
                            for (String dat : mapM.keySet()) {
                                int i1 = -1;
                                for (Mark marksM : mapM.get(dat)) {
                                    body.wrtr.name(i1 == -1 ? dat : (dat + "," + i1)).beginObject()
                                        .name("mark").value(marksM.getMark())
                                        .name("weight").value(marksM.getWeight())
                                        .name("type").value(marksM.getStyle())
                                        .endObject();
                                    i1++;
                                }
                            }
                        }
                        body.wrtr.endObject().endObject();
                    }
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.JOURNAL, ref.sch +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataJournal {
    public String uuid;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}