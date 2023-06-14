package ru.mirea.services;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import ru.mirea.Main;
import ru.mirea.controllers.CallInterface;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.data.models.school.Request;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.school.day.Day;
import ru.mirea.data.models.school.day.Mark;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.NewsRepository;
import ru.mirea.data.reps.SystRepository;
import ru.mirea.data.reps.auth.InviteRepository;
import ru.mirea.data.reps.auth.RoleRepository;
import ru.mirea.data.reps.auth.SettingUserRepository;
import ru.mirea.data.reps.auth.UserRepository;
import ru.mirea.data.reps.school.*;
import ru.mirea.data.reps.school.day.DayRepository;
import ru.mirea.data.reps.school.day.MarkRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Getter
@Service public class ServerService {

    private final UserRepository userRepository;

    private final InviteRepository inviteRepository;

    private final SchoolRepository schoolRepository;

    private final RequestRepository requestRepository;

    private final SystRepository systRepository;

    private final NewsRepository newsRepository;

    private final ContactsRepository contactsRepository;

    private final GroupRepository groupRepository;

    private final DayRepository dayRepository;

    private final LessonRepository lessonRepository;

    private final MarkRepository markRepository;

    private final SettingUserRepository settingUserRepository;

    private final PeriodRepository periodRepository;

    private final RoleRepository roleRepository;

    private final JsonObject errObj = new JsonObject();

    @Autowired
    private PushService pushService;

    public ServerService(UserRepository userRepository, InviteRepository inviteRepository, SchoolRepository schoolRepository, RequestRepository requestRepository, SystRepository systRepository, NewsRepository newsRepository, ContactsRepository contactsRepository, GroupRepository groupRepository, DayRepository dayRepository, LessonRepository lessonRepository, MarkRepository markRepository, SettingUserRepository settingUserRepository, PeriodRepository periodRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.schoolRepository = schoolRepository;
        this.requestRepository = requestRepository;
        this.systRepository = systRepository;
        this.newsRepository = newsRepository;
        this.contactsRepository = contactsRepository;
        this.groupRepository = groupRepository;
        this.dayRepository = dayRepository;
        this.lessonRepository = lessonRepository;
        this.markRepository = markRepository;
        this.settingUserRepository = settingUserRepository;
        this.periodRepository = periodRepository;
        this.roleRepository = roleRepository;

        errObj.addProperty("error", true);

        new IniDB(this);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public SettingUser createSettingUser(SettingUser settingUser) {
        SettingUser savedSettingUser = settingUserRepository.saveAndFlush(settingUser);
        System.out.println(savedSettingUser);
        return savedSettingUser;
    }

    public void addToken(SettingUser settingUser, String token) {
        settingUser.getTokens().add(token);
        settingUser.getTopics().forEach((topic) -> {
            if (settingUser.getNotif()
                    && ((topic.contains("News") && settingUser.getNNewNewsYO())
                    || (topic.contains("news") && settingUser.getNNewNewsPor()))) {
                if (pushService.subscribe(asList(token), topic) > 0) {
                    settingUser.getTokens().remove(token);
                }
            }
        });
    }

    public void remToken(SettingUser settingUser, String token) {
        settingUser.getTokens().remove(token);
        settingUser.getTopics().forEach((topic) -> {
            pushService.unsubscribe(asList(token), topic);
        });
    }

    public void addTopic(SettingUser settingUser, String topic) {
        settingUser.getTopics().add(topic);
//        pushService.subscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }

    public void remTopic(SettingUser settingUser, String topic) {
        settingUser.getTopics().remove(topic);
//        pushService.unsubscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }

    public User userByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User userByCode(String code) {
        return userRepository.findByCode(code);
    }

    public User userById(Long id) {
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    public void usersByList(List<Long> list, JsonWriter wrtr, boolean lin) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Long i : list) {
            User objU = userById(i);
            if (objU == null) continue;
            wrtr.name(i + "").beginObject()
                .name("name").value(objU.getFio())
                .name("login").value(objU.getLogin());
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
                .name("name").value(objU.getFio())
                .name("login").value(objU.getLogin());
            if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
                wrtr.name("link").value(objU.getCode());
            }
            wrtr.endObject();
        }
    }

    public Long getFirstRoleId(Map<Long, Role> map) {
        return (Long) map.keySet().toArray()[0];
    }

    public Role getFirstRole(Map<Long, Role> map) {
        return map.get(getFirstRoleId(map));
    }

    public Invite createInvite(Invite inv) {
        Invite savedInv = inviteRepository.saveAndFlush(inv);
        System.out.println(savedInv);
        return savedInv;
    }

    public List<Invite> getInvites() {
        return inviteRepository.findAll();
    }

    public Invite inviteByCode(String code) {
        return inviteRepository.findByCode(code);
    }

    public Invite inviteById(Long id) {
        return id == null ? null : inviteRepository.findById(id).orElse(null);
    }

    public void invitesByList(List<Long> list, JsonWriter wrtr, boolean lin) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Long i : list) {
            Invite objI = inviteById(i);
            if (objI == null) continue;
            wrtr.name(i + "").beginObject()
                    .name("name").value(objI.getFio());
            if (lin && !ObjectUtils.isEmpty(objI.getCode())) {
                wrtr.name("link").value(objI.getCode());
            }
            wrtr.endObject();
        }
    }

    public void invitesByList(List<Invite> list, boolean lin, JsonWriter wrtr) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Invite objI : list) {
            if (objI == null) continue;
            wrtr.name(objI.getId() + "").beginObject()
                    .name("name").value(objI.getFio());
            if (lin && !ObjectUtils.isEmpty(objI.getCode())) {
                wrtr.name("link").value(objI.getCode());
            }
            wrtr.endObject();
        }
    }

    public List<Request> createReq(@RequestBody Request request) {
        Request savedRequest = requestRepository.saveAndFlush(request);
        System.out.println(savedRequest);
        return requestRepository.findAll();
    }

    public List<Request> getRequests() {
        return requestRepository.findAll();
    }

    public Request requestById(Long id) {
        return id == null ? null : requestRepository.findById(id).orElse(null);
    }

    public List<School> getSchools() {
        return schoolRepository.findAll();
    }

    public School schoolById(Long id) {
        return id == null ? null : schoolRepository.findById(id).orElse(null);
    }

    public Syst createSyst(Syst syst) {
        Syst savedSyst = systRepository.saveAndFlush(syst);
        System.out.println(savedSyst);
        return savedSyst;
    }

    public Syst getSyst() {
        List<Syst> systs = systRepository.findAll();
        return systs.isEmpty() ? null : systs.get(0);
    }

    public News newsById(Long id) {
        return id == null ? null : newsRepository.findById(id).orElse(null);
    }

    public Contacts contactsById(Long id) {
        return id == null ? null : contactsRepository.findById(id).orElse(null);
    }

    public Group groupById(Long id) {
        return id == null ? null : groupRepository.findById(id).orElse(null);
    }

    public Long groupsByUser(User user, JsonWriter wrtr) throws Exception {
        Long first = null;
        if (user != null) {
            School school = getFirstRole(user.getRoles()).getYO();
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
        invitesByList(school.getTeachersInv(), true, wrtr);
        wrtr.endObject().endObject();
        List<Object[]> lessU = getLessonRepository().uniqTeachersBySchool(4L);
        List<Object[]> lessI = getLessonRepository().uniqTeachersInvBySchool(4L);
        if (!ObjectUtils.isEmpty(lessU) && !ObjectUtils.isEmpty(lessI)) {
            Map<String, List<Long>> mapU = lessU.stream().collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
            ));
            Map<String, List<Long>> mapI = lessI.stream().collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
            ));
            HashSet<String> keys = new HashSet<>(mapU.keySet());
            keys.addAll(mapI.keySet());
            int i = 0;
            for (String subName : keys) {
                wrtr.name(i + "").beginObject()
                    .name("name").value(subName)
                    .name("tea").beginObject();
                if (mapU.containsKey(subName)) {
                    usersByList(mapU.get(subName), wrtr, true);
                }
                if (mapI.containsKey(subName)) {
                    invitesByList(mapI.get(subName), wrtr, true);
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
        Long schId = getFirstRole(user.getRoles()).getYO().getId();
        List<Lesson> lessons;
        if (user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
            lessons = getLessonRepository().findBySchoolAndTeacher(schId, user.getId());
        } else {
            lessons = getLessonRepository().findBySchoolAndGrp(schId, gId);
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
                Invite teaI = les.getTeacherInv();
                wrtr.name("prepod").beginObject();
                if (teaU != null) {
                    wrtr.name("name").value(teaU.getFio())
                            .name("id").value(teaU.getId());
                } else if (teaI != null) {
                    wrtr.name("name").value(teaI.getFio())
                            .name("id").value(teaI.getId());
                }
                wrtr.endObject();
            }
            wrtr.endObject();
        }
        if (k1 != -1) wrtr.endObject().endObject();
        wrtr.endObject();
    }

    public void createMark(Mark mark) {
        Mark savedMark = markRepository.saveAndFlush(mark);
        System.out.println(savedMark);
    }

    public void createDay(Day day) {
        Day savedDay = dayRepository.saveAndFlush(day);
        System.out.println(savedDay);
    }
}