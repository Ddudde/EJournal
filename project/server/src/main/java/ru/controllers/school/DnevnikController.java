package ru.controllers.school;

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
import ru.Main;
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.User;
import ru.data.models.school.*;
import ru.services.MainService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequestMapping("/dnevnik")
@NoArgsConstructor
@RestController public class DnevnikController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/getDnevnik")
    public JsonObject getDnevnik(@RequestBody DataDnevnik body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Group group = null;
            School school = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                if(user.getSelRole() == 0L && user.getRoles().containsKey(0L)) {
                    ref.group = user.getRoles().get(0L).getGrp();
                } else if(user.getSelRole() == 1L && user.getRoles().containsKey(1L)) {
                    User kidU = datas.getDbService().userById(user.getSelKid());
                    if(kidU != null) {
                        ref.group = kidU.getRoles().get(0L).getGrp();
                    }
                }
                if (ref.group != null || user.getSelRole() == 2L) {
                    ref.school = datas.getDbService().getFirstRole(user.getRoles()).getYO();
                    List<Lesson> lessons;
                    if(user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
                        lessons = datas.getDbService().getLessonRepository().findBySchoolIdAndTeacherId(ref.school.getId(), user.getId());
                    } else {
                        lessons = datas.getDbService().getLessonRepository().findBySchoolIdAndGrpId(ref.school.getId(), ref.group.getId());
                    }
                    body.wrtr.name("body").beginObject();
                    lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
                    int k1 = -1;
                    for(Lesson les : lessons) {
                        if(k1 != les.getDayWeek()) {
                            if(k1 != -1) body.wrtr.endObject().endObject();
                            k1 = les.getDayWeek();
                            body.wrtr.name(k1 + "").beginObject()
                                .name("lessons").beginObject();
                        }
                        body.wrtr.name(les.getNumLesson() + "").beginObject();
                        if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                            body.wrtr.name("name").value(les.getNameSubject());
                        }
                        body.wrtr.name("cabinet").value(les.getKab());
                        if(user.getSelRole() == 2L) {
                            Group grp = les.getGrp();
                            body.wrtr.name("group").value(grp.getName());
                        } else {
                            User teaU = les.getTeacher();
                            body.wrtr.name("prepod").beginObject();
                            if (teaU != null) {
                                body.wrtr.name("name").value(teaU.getFio())
                                    .name("id").value(teaU.getId());
                            }
                            body.wrtr.endObject();
                        }
                        body.wrtr.endObject();
                    }
                    if(k1 != -1) body.wrtr.endObject().endObject();
                    body.wrtr.endObject();

                    Period actPeriod = datas.getActualPeriodBySchool(ref.school);
                    List<Object[]> marks = datas.getDbService().getDayRepository().uniqNameSubjectAndDatAndMarksByParams(ref.school.getId(), ref.group.getId(), actPeriod.getId());
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
                    body.wrtr.name("min").value(actPeriod.getDateN());
                    body.wrtr.name("max").value(actPeriod.getDateK());
                    List<Object[]> homeworks = datas.getDbService().getDayRepository().uniqNameSubAndDatAndHomeworkByParams(ref.school.getId(), ref.group.getId());
                    Map<String, Map<String, String>> mapH = homeworks.stream().collect(Collectors.groupingBy(
                        obj -> (String) obj[0],
                        Collectors.toMap(obj -> (String) obj[1], obj -> (String) obj[2])
                    ));
                    body.wrtr.name("bodyD").beginObject();
                    if(!ObjectUtils.isEmpty(mapD)) {
                        for (String nameSub : mapD.keySet()) {
                            Map<String, List<Mark>> mapM = mapD.get(nameSub);
                            body.wrtr.name(nameSub).beginObject();
                            if (!ObjectUtils.isEmpty(mapM)) {
                                for (String dat : mapM.keySet()) {
                                    body.wrtr.name(dat).beginObject();
                                    int i1 = 0;
                                    for (Mark marksM : mapM.get(dat)) {
                                        body.wrtr.name(i1++ + "").beginObject()
                                            .name("mark").value(marksM.getMark())
                                            .name("weight").value(marksM.getWeight())
                                            .name("type").value(marksM.getStyle());
                                        if (i1 == 1 && !ObjectUtils.isEmpty(mapH) && mapH.containsKey(nameSub) && mapH.get(nameSub).containsKey(dat)) {
                                            body.wrtr.name("homework").value(mapH.get(nameSub).get(dat));
                                        }
                                        body.wrtr.endObject();
                                    }
                                    body.wrtr.name("i").value(0)
                                        .endObject();
                                }
                            }
                            body.wrtr.endObject();
                        }
                    } else if(!ObjectUtils.isEmpty(mapH)) {
                        for (String nameSub : mapH.keySet()) {
                            body.wrtr.name(nameSub).beginObject();
                            if (!ObjectUtils.isEmpty(mapH.get(nameSub))) {
                                for (String dat : mapH.get(nameSub).keySet()) {
                                    body.wrtr.name(dat).beginObject()
                                        .name("0").beginObject();
                                    if (mapH.get(nameSub).containsKey(dat)) {
                                        body.wrtr.name("homework").value(mapH.get(nameSub).get(dat));
                                    }
                                    body.wrtr.endObject()
                                        .name("i").value(0)
                                        .endObject();
                                }
                            }
                            body.wrtr.endObject();
                        }
                    }
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            boolean b = user.getSelRole() == 2L || user.getSelRole() == 3L;
            authController.infCon(body.uuid, null, null, null, b ? null : ref.group.getId()+"", null, null);
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataDnevnik body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null) {
                ref.schId = datas.getDbService().getFirstRole(user.getRoles()).getYO().getId();
                if(user.getRoles().containsKey(0L) || user.getRoles().containsKey(1L)) {
                    body.wrtr.name("yes").value(true);
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.DNEVNIK, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataDnevnik {
    public String uuid;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}