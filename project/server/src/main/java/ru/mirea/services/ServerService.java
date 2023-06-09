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
import ru.mirea.data.json.Role;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.data.models.school.day.Day;
import ru.mirea.data.models.school.day.Mark;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.NewsRepository;
import ru.mirea.data.reps.SystRepository;
import ru.mirea.data.reps.auth.InviteRepository;
import ru.mirea.data.reps.auth.SettingUserRepository;
import ru.mirea.data.reps.auth.UserRepository;
import ru.mirea.data.reps.school.*;
import ru.mirea.data.reps.school.day.DayRepository;
import ru.mirea.data.reps.school.day.MarkRepository;

import java.io.IOException;
import java.util.*;
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

    private final JsonObject errObj = new JsonObject();

    @Autowired
    private PushService pushService;

    public ServerService(UserRepository userRepository, InviteRepository inviteRepository, SchoolRepository schoolRepository, RequestRepository requestRepository, SystRepository systRepository, NewsRepository newsRepository, ContactsRepository contactsRepository, GroupRepository groupRepository, DayRepository dayRepository, LessonRepository lessonRepository, MarkRepository markRepository, SettingUserRepository settingUserRepository, PeriodRepository periodRepository) {
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

        errObj.addProperty("error", true);

        new IniDB(this);
    }

    public void createUser(User user) {
        User savedUser = userRepository.saveAndFlush(user);
        System.out.println(savedUser);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public SettingUser createSettingUser(SettingUser settingUser) {
        SettingUser savedSettingUser = settingUserRepository.saveAndFlush(settingUser);
        System.out.println(savedSettingUser);
        return savedSettingUser;
    }

    public SettingUser settingUserById(Long id) {
        settingUserRepository.findById(id);
        return id == null ? null : settingUserRepository.findById(id).orElse(null);
    }

    public void addToken(SettingUser settingUser, String token) {
        settingUser.getTokens().add(token);
        settingUser.getTopics().forEach((topic) -> {
            if(settingUser.getNotif()
            && ((topic.contains("News") && settingUser.getNNewNewsYO())
            || (topic.contains("news") && settingUser.getNNewNewsPor()))) {
                if (pushService.subscribe(asList(token), topic) > 0) {
                    settingUser.getTokens().remove(token);
                }
            }
        });
    }

    public void remToken(SettingUser settingUser, String token){
        settingUser.getTokens().remove(token);
        settingUser.getTopics().forEach((topic) -> {
            pushService.unsubscribe(asList(token), topic);
        });
    }

    public void addTopic(SettingUser settingUser, String topic) {
        settingUser.getTopics().add(topic);
//        pushService.subscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }

    public void remTopic(SettingUser settingUser, String topic){
        settingUser.getTopics().remove(topic);
//        pushService.unsubscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }

    public User userByLogin(String login){
        return userRepository.findByLogin(login);
    }

    public User userByCode(String code){
        return userRepository.findByCode(code);
    }

    public User userById(Long id){
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    public void usersByList(List<Long> list, JsonObject obj){
        for (Long i : list) {
            JsonObject objO = new JsonObject();
            User objU = userById(i);
            objO.addProperty("name", objU.getFio());
            objO.addProperty("login", objU.getLogin());
            if (!ObjectUtils.isEmpty(objU.getCode())) {
                objO.addProperty("link", objU.getCode());
            }
            obj.add(i + "", objO);
        }
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

    public Long getFirstRoleId(Map<Long, Role> map){
        return (Long) map.keySet().toArray()[0];
    }

    public Role getFirstRole(Map<Long, Role> map){
        return map.get(getFirstRoleId(map));
    }

    public void createInvite(Invite inv) {
        Invite savedInv = inviteRepository.saveAndFlush(inv);
        System.out.println(savedInv);
    }

    public List<Invite> getInvites() {
        return inviteRepository.findAll();
    }

    public Invite inviteByCode(String code){
        return inviteRepository.findByCode(code);
    }

    public Invite inviteById(Long id){
        return id == null ? null : inviteRepository.findById(id).orElse(null);
    }

    public void invitesByList(List<Long> list, JsonObject obj){
        for (Long i : list) {
            JsonObject objO = new JsonObject();
            Invite objI = inviteById(i);
            objO.addProperty("name", objI.getFio());
            if (!ObjectUtils.isEmpty(objI.getCode())) {
                objO.addProperty("link", objI.getCode());
            }
            obj.add(i + "", objO);
        }
    }

    public void invitesByList(List<Long> list, JsonWriter wrtr, boolean lin) throws Exception {
        if (ObjectUtils.isEmpty(list)) return;
        for (Long i : list) {
            Invite objI = inviteById(i);
            if(objI == null) continue;
            wrtr.name(i + "").beginObject()
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

    public Request requestById(Long id){
        return id == null ? null : requestRepository.findById(id).orElse(null);
    }

    public void createSchool(School school) {
        School savedSchool = schoolRepository.saveAndFlush(school);
        System.out.println(savedSchool);
    }

    public List<School> getSchools() {
        return schoolRepository.findAll();
    }

    public School schoolById(Long id){
        return id == null ? null : schoolRepository.findById(id).orElse(null);
    }

    public void createSyst(Syst syst) {
        Syst savedSyst = systRepository.saveAndFlush(syst);
        System.out.println(savedSyst);
    }

    public Syst getSyst() {
        List<Syst> systs = systRepository.findAll();
        return systs.isEmpty() ? null : systs.get(0);
    }

    public void createNews(News news) {
        News savedNews = newsRepository.saveAndFlush(news);
        System.out.println(savedNews);
    }

    public List<News> getNews() {
        return newsRepository.findAll();
    }

    public News newsById(Long id){
        return id == null ? null : newsRepository.findById(id).orElse(null);
    }

    public void createContacts(Contacts contacts) {
        Contacts savedContacts = contactsRepository.saveAndFlush(contacts);
        System.out.println(savedContacts);
    }

    public List<Contacts> getContacts() {
        return contactsRepository.findAll();
    }

    public Contacts contactsById(Long id){
        return id == null ? null : contactsRepository.findById(id).orElse(null);
    }

    public void createGroup(Group group) {
        Group savedGroup = groupRepository.saveAndFlush(group);
        System.out.println(savedGroup);
    }

    public List<Group> getGroups() {
        return groupRepository.findAll();
    }

    public Group groupById(Long id){
        return id == null ? null : groupRepository.findById(id).orElse(null);
    }

    public Long groupsByUser(User user, JsonObject bodyAns){
        Long first = null;
        if(user != null){
            Long schId = getFirstRole(user.getRoles()).getYO();
            School school = schoolById(schId);
            if (!ObjectUtils.isEmpty(school.getGroups())) {
                first = school.getGroups().get(0);
                for (Long i : school.getGroups()) {
                    Group gr = groupById(i);
                    bodyAns.addProperty(i + "", gr.getName());
                }
            }
        }
        return first;
    }

    public Long groupsByUser(User user, JsonWriter wrtr) throws Exception {
        Long first = null;
        if(user != null){
            Long schId = getFirstRole(user.getRoles()).getYO();
            School school = schoolById(schId);
            if (!ObjectUtils.isEmpty(school.getGroups())) {
                first = school.getGroups().get(0);
                for (Long i : school.getGroups()) {
                    Group gr = groupById(i);
                    wrtr.name(i + "").value(gr.getName());
                }
            }
        }
        wrtr.endObject();
        return first;
    }

    public void teachersBySchool(School school, JsonWriter wrtr) throws Exception {
        wrtr.name("nt").beginObject().name("tea").beginObject();
        usersByList(school.getTeachers(), wrtr, true);
        invitesByList(school.getTeachersInv(), wrtr, true);
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
            for(String subName : keys){
                wrtr.name(i + "").beginObject()
                    .name("name").value(subName)
                    .name("tea").beginObject();
                if(mapU.containsKey(subName)) {
                    usersByList(mapU.get(subName), wrtr, true);
                }
                if(mapI.containsKey(subName)) {
                    invitesByList(mapI.get(subName), wrtr, true);
                }
                wrtr.endObject().endObject();
                i++;
            }
        }
        wrtr.endObject();
    }

    public void createLesson(Lesson lesson) {
        Lesson savedLesson = lessonRepository.saveAndFlush(lesson);
        System.out.println(savedLesson);
    }

    public List<Lesson> getLessons() {
        return lessonRepository.findAll();
    }

    public Lesson lessonById(Long id){
        return id == null ? null : lessonRepository.findById(id).orElse(null);
    }

    public JsonObject getObj(CallInterface callable, JsonTreeWriter wrtr, boolean bol) {
        JsonObject ans = null;
        try {
            wrtr.endObject();
            ans = wrtr.get().getAsJsonObject();
            System.out.println("dsf" + ans);
            wrtr.close();
        } catch (Exception e) {bol = Main.excp(e);}
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
        Long schId = getFirstRole(user.getRoles()).getYO();
        List<Lesson> lessons;
        if(user.getSelRole() == 2L && user.getRoles().containsKey(2L)) {
            lessons = getLessonRepository().findBySchoolAndTeacher(schId, user.getId());
        } else {
            lessons = getLessonRepository().findBySchoolAndGrp(schId, gId);
        }
        wrtr.name(nameWrtr).beginObject();
        lessons.sort(Comparator.comparing(Lesson::getDayWeek).thenComparing(Lesson::getNumLesson));
        int k1 = -1;
        for(Lesson les : lessons) {
            if(k1 != les.getDayWeek()) {
                if(k1 != -1) wrtr.endObject().endObject();
                k1 = les.getDayWeek();
                wrtr.name(k1 + "").beginObject()
                    .name("lessons").beginObject();
            }
            wrtr.name(les.getNumLesson() + "").beginObject();
            if (!ObjectUtils.isEmpty(les.getNameSubject())) {
                wrtr.name("name").value(les.getNameSubject());
            }
            wrtr.name("cabinet").value(les.getKab());
            if(user.getSelRole() == 2L) {
                Group grp = groupById(les.getGrp());
                wrtr.name("group").value(grp.getName());
            } else {
                User teaU = userById(les.getTeacher());
                Invite teaI = inviteById(les.getTeacherInv());
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
        if(k1 != -1) wrtr.endObject().endObject();
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

    public void createPeriod(Period day) {
        Period savedPeriod = periodRepository.saveAndFlush(day);
        System.out.println(savedPeriod);
    }

    public Period periodById(Long id){
        return id == null ? null : periodRepository.findById(id).orElse(null);
    }

}
