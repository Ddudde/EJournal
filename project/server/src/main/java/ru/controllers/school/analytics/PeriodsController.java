package ru.controllers.school.analytics;

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
import ru.data.models.school.Period;
import ru.data.models.school.School;
import ru.services.MainService;

@RequestMapping("/periods")
@NoArgsConstructor
@RestController public class PeriodsController {

    @Autowired
    private MainService datas;

    @Autowired
    private AuthController authController;

    @PostMapping(value = "/addPer")
    public JsonObject addPer(@RequestBody DataPeriods body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                School school = user.getRoles().get(3L).getYO();
                ref.schId = school.getId();
                Period period = new Period();
                period.setName(body.name);
                period.setDateN(body.perN);
                period.setDateK(body.perK);
                datas.getDbService().getPeriodRepository().saveAndFlush(period);
                school.getPeriods().add(period);
                datas.getDbService().getSchoolRepository().saveAndFlush(school);
                body.wrtr.name("id").value(period.getId())
                    .name("body").beginObject()
                    .name("name").value(period.getName())
                    .name("perN").value(period.getDateN())
                    .name("perK").value(period.getDateK())
                    .endObject();
            }
        } catch (Exception e) {body.bol = Main.excp(e);}
        return datas.getObj(ans -> {
            authController.sendEventFor("addPerC", ans, TypesConnect.PERIODS, ref.schId +"", "main", "main", "main");
        }, body.wrtr, body.bol);
    }

    @PostMapping(value = "/getInfo")
    public JsonObject getInfo(@RequestBody DataPeriods body) {
        Subscriber subscriber = authController.getSubscriber(body.uuid);
        User user = datas.getDbService().userByLogin(subscriber.getLogin());
        final var ref = new Object() {
            Long schId = null;
        };
        try {
            body.wrtr = datas.init(body.toString());
            if(user != null && user.getRoles().containsKey(3L)) {
                School school = user.getRoles().get(3L).getYO();
                ref.schId = school.getId();
                if (!ObjectUtils.isEmpty(school.getPeriods())){
                    System.out.println(school.getPeriods());
                    body.wrtr.name("bodyP").beginObject();
                    int i = 0;
                    for (Period period : school.getPeriods()) {
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