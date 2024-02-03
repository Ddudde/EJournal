package ru.mirea.services;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
import ru.mirea.controllers.CallInterface;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.services.db.DBService;
import ru.mirea.services.db.IniDBService;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Service public class ServerService {

    private final JsonObject errObj = new JsonObject();

    @Autowired
    private PushService pushService;

    private final IniDBService iniDBService;

    private final DBService dbService;

    @Autowired
    private EmailService emailService;

    public ServerService(DBService dbService, IniDBService iniDBService) {
        this.dbService = dbService;
        errObj.addProperty("error", true);
        iniDBService.firstIni(this);
        this.iniDBService = iniDBService;
        iniDBService.checkDates();
    }

    public void usersByList(List<Long> list, JsonWriter wrtr, boolean lin) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Long i : list) {
            User objU = dbService.userById(i);
            if (objU == null) continue;
            wrtr.name(i + "").beginObject()
                .name("name").value(objU.getFio());
            if (!ObjectUtils.isEmpty(objU.getUsername())) {
                wrtr.name("login").value(objU.getUsername());
            }
            if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
                wrtr.name("link").value(objU.getCode());
            }
            wrtr.endObject();
        }
    }

    public void usersByList(List<User> list, boolean lin, JsonWriter wrtr) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (User objU : list) {
            if (objU == null) continue;
            wrtr.name(objU.getId() + "").beginObject()
                .name("name").value(objU.getFio());
            if (!ObjectUtils.isEmpty(objU.getUsername())) {
                wrtr.name("login").value(objU.getUsername());
            }
            if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
                wrtr.name("link").value(objU.getCode());
            }
            wrtr.endObject();
        }
    }

    public Long groupsByUser(User user, JsonWriter wrtr) throws Exception {
        Long first = null;
        if (user != null) {
            School school = dbService.getFirstRole(user.getRoles()).getYO();
            if (!ObjectUtils.isEmpty(school.getGroups())) {
                first = school.getGroups().get(0).getId();
                for (Group gr : school.getGroups()) {
                    wrtr.name(gr.getId() + "").value(gr.getName());
                }
            }
        }
        wrtr.endObject();
        return first;
    }

    public void teachersBySchool(School school, JsonWriter wrtr) throws Exception {
        wrtr.name("nt").beginObject().name("tea").beginObject();
        usersByList(school.getTeachers(), true, wrtr);
        wrtr.endObject().endObject();
        List<Object[]> lessU = dbService.getLessonRepository().uniqTeachersLBySchool(school.getId());
        if (!ObjectUtils.isEmpty(lessU)) {
            Map<String, List<Long>> mapU = lessU.stream().collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
            ));
            System.out.println("testM " + mapU);
            int i = 0;
            for (String subName : mapU.keySet()) {
                wrtr.name(i + "").beginObject()
                    .name("name").value(subName)
                    .name("tea").beginObject();
                if (mapU.containsKey(subName)) {
                    usersByList(mapU.get(subName), wrtr, true);
                }
                wrtr.endObject().endObject();
                i++;
            }
        }
        wrtr.endObject();
    }

    public JsonObject getObj(CallInterface callable, JsonTreeWriter wrtr, boolean bol) {
        JsonObject ans = null;
        try {
            wrtr.endObject();
            ans = wrtr.get().getAsJsonObject();
            System.out.println("dsf" + ans);
            wrtr.close();
        } catch (Exception e) {
            bol = Main.excp(e);
        }
        if (ans != null && ans.keySet().size() > 1 && bol) {
            callable.call(ans);
        } else {
            ans = errObj;
        }
        return ans;
    }

    public JsonTreeWriter ini(String data) throws Exception {
        System.out.println("Post! " + data);
        JsonTreeWriter wrtr = new JsonTreeWriter();
        wrtr.beginObject().name("error").value(false);
        return wrtr;
    }

    public void getShedule(String nameWrtr, User user, JsonTreeWriter wrtr, Long gId) throws Exception {
        Long schId = dbService.getFirstRole(user.getRoles()).getYO().getId();
        List<Lesson> lessons;
        if (user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
            lessons = dbService.getLessonRepository().findBySchoolIdAndTeacherId(schId, user.getId());
        } else {
            lessons = dbService.getLessonRepository().findBySchoolIdAndGrpId(schId, gId);
        }
        wrtr.name(nameWrtr).beginObject();
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        int k1 = -1;
        for (Lesson les : lessons) {
            if (k1 != les.getDayWeek()) {
                if (k1 != -1) wrtr.endObject().endObject();
                k1 = les.getDayWeek();
                wrtr.name(k1 + "").beginObject()
                    .name("lessons").beginObject();
            }
            wrtr.name(les.getNumLesson() + "").beginObject();
            if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                wrtr.name("name").value(les.getNameSubject());
            }
            wrtr.name("cabinet").value(les.getKab());
            if (user.getSelRole() == 2L) {
                Group grp = les.getGrp();
                wrtr.name("group").value(grp.getName());
            } else {
                User teaU = les.getTeacher();
                wrtr.name("prepod").beginObject();
                if (teaU != null) {
                    wrtr.name("name").value(teaU.getFio())
                        .name("id").value(teaU.getId());
                }
                wrtr.endObject();
            }
            wrtr.endObject();
        }
        if (k1 != -1) wrtr.endObject().endObject();
        wrtr.endObject();
    }

    private void getUsersT(JsonTreeWriter wrtr, List<User> users) throws Exception {
        for (User user : users) {
            wrtr.name(user.getId()+"").beginObject()
                .name("fio").value(user.getFio())
                .name("login").value(user.getUsername())
                .name("pass").value(user.getPassword())
                .name("code").value(user.getCode())
                .endObject();
        }
    }

    public void getTestInfo(JsonTreeWriter wrtr) throws Exception {
        wrtr.name("bodyT").beginObject()
            .name("admins").beginObject();
        if(iniDBService.getSyst() != null) getUsersT(wrtr, iniDBService.getSyst().getAdmins());
        wrtr.endObject()
            .name("schools").beginObject();
        if(iniDBService.getSchools() != null) {
            for (School school : iniDBService.getSchools()) {
                wrtr.name(school.getId()+"").beginObject()
                    .name("name").value(school.getName())
                    .name("hteachers").beginObject();
                getUsersT(wrtr, school.getHteachers());
                wrtr.endObject()
                    .name("teachers").beginObject();
                List<User> teachersUBySchool = dbService.getLessonRepository().uniqTeachersUBySchool(school.getId());
                getUsersT(wrtr, teachersUBySchool);
                wrtr.endObject()
                    .name("groups").beginObject();
                for (Group group : school.getGroups()) {
                    wrtr.name(group.getId()+"").beginObject()
                        .name("name").value(group.getName())
                        .name("kids").beginObject();
                    getUsersT(wrtr, group.getKids());
                    wrtr.endObject()
                        .name("parents").beginObject();
                    for (User user : group.getKids()) {
                        if(!user.getRoles().containsKey(0L)) continue;
                        getUsersT(wrtr, user.getRole(0L).getParents());
                    }
                    wrtr.endObject()
                        .endObject();
                }
                wrtr.endObject()
                    .endObject();
            }
        }
        wrtr.endObject()
            .endObject();
    }

    public Period getActualPeriodBySchool(School school) {
        try {
            long now = Main.df.parse(Main.df.format(new Date())).getTime();
            for (Period per : school.getPeriods()) {
                if (now >= Main.df.parse(per.getDateN()).getTime() && now <= Main.df.parse(per.getDateK()).getTime()) {
                    return per;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}