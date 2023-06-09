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
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.data.models.school.Period;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.util.*;

@RequestMapping("/periods")
@NoArgsConstructor
@RestController public class PeriodsController {

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addPer")
    public JsonObject addPer(@RequestBody DataPeriods body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                ref.schId = user.getRoles().get(3L).getYO();
                School school = datas.schoolById(ref.schId);
                Period period = new Period();
                period.setName(body.name);
                period.setDateN(body.perN);
                period.setDateK(body.perK);
                datas.getPeriodRepository().saveAndFlush(period);
                school.getPeriods().add(period.getId());
                datas.getSchoolRepository().saveAndFlush(school);
                body.wrtr.name("id").value(period.getId())
                    .name("body").beginObject()
                    .name("name").value(period.getName())
                    .name("perN").value(period.getDateN())
                    .name("perK").value(period.getDateK())
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendMessageForAll("addPerC", ans, TypesConnect.PERIODS, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataPeriods body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.ini(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                ref.schId = user.getRoles().get(3L).getYO();
                School school = datas.schoolById(ref.schId);
                if (!ObjectUtils.isEmpty(school.getPeriods())){
                    System.out.println(school.getPeriods());
                    body.wrtr.name("bodyP").beginObject();
                    int i = 0;
                    for (Long id : school.getPeriods()) {
                        Period period = datas.periodById(id);
                        if(period == null) continue;
                        body.wrtr.name(i+"").beginObject()
                            .name("name").value(period.getName())
                            .name("perN").value(period.getDateN())
                            .name("perK").value(period.getDateK())
                            .endObject();
                        i++;
                    }
                    body.wrtr.endObject();
                }
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.infCon(body.uuid, null, TypesConnect.PERIODS, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }
}

@ToString
@NoArgsConstructor @AllArgsConstructor
class DataPeriods {
    public String uuid, name, perN, perK;
    public transient boolean bol = true;
    public transient JsonTreeWriter wrtr;
}