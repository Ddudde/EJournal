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
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.Request;
import ru.mirea.data.models.school.School;
import ru.mirea.data.models.school.dayOfWeek.DayOfWeek;
import ru.mirea.data.models.school.dayOfWeek.Lesson;
import ru.mirea.data.models.school.dayOfWeek.Subject;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.NewsRepository;
import ru.mirea.data.reps.SystRepository;
import ru.mirea.data.reps.auth.InviteRepository;
import ru.mirea.data.reps.auth.UserRepository;
import ru.mirea.data.reps.school.GroupRepository;
import ru.mirea.data.reps.school.RequestRepository;
import ru.mirea.data.reps.school.SchoolRepository;
import ru.mirea.data.reps.school.day.DayRepository;
import ru.mirea.data.reps.school.day.MarkRepository;
import ru.mirea.data.reps.school.dayOfWeek.DayOfWeekRepository;
import ru.mirea.data.reps.school.dayOfWeek.LessonRepository;
import ru.mirea.data.reps.school.dayOfWeek.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final DayOfWeekRepository dayOfWeekRepository;

    private final DayRepository dayRepository;

    private final LessonRepository lessonRepository;

    private final MarkRepository markRepository;

    private final SubjectRepository subjectRepository;

    private final JsonObject errObj = new JsonObject();

    @Autowired
    private PushService pushService;

    public ServerService(UserRepository userRepository, InviteRepository inviteRepository, SchoolRepository schoolRepository, RequestRepository requestRepository, SystRepository systRepository, NewsRepository newsRepository, ContactsRepository contactsRepository, GroupRepository groupRepository, DayOfWeekRepository dayOfWeekRepository, DayRepository dayRepository, LessonRepository lessonRepository, MarkRepository markRepository, SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.schoolRepository = schoolRepository;
        this.requestRepository = requestRepository;
        this.systRepository = systRepository;
        this.newsRepository = newsRepository;
        this.contactsRepository = contactsRepository;
        this.groupRepository = groupRepository;
        this.dayOfWeekRepository = dayOfWeekRepository;
        this.dayRepository = dayRepository;
        this.lessonRepository = lessonRepository;
        this.markRepository = markRepository;
        this.subjectRepository = subjectRepository;

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

    public void addToken(User user, String token) {
        user.getTokens().add(token);
        user.getTopics().forEach((topic) -> {
            if(pushService.subscribe(asList(token), topic) > 0) {
                user.getTokens().remove(token);
            }
        });
    }

    public void remToken(User user, String token){
        user.getTokens().remove(token);
        user.getTopics().forEach((topic) -> {
            pushService.unsubscribe(asList(token), topic);
        });
    }

    public void addTopic(User user, String topic) {
        user.getTopics().add(topic);
        pushService.subscribe(new ArrayList<>(user.getTokens()), topic);
    }

    public void remTopic(User user, String topic){
        user.getTopics().remove(topic);
        pushService.unsubscribe(new ArrayList<>(user.getTokens()), topic);
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
            if (!ObjectUtils.isEmpty(objU.getCode())) objO.addProperty("link", objU.getCode());
            obj.add(i + "", objO);
        }
    }

    public void usersByList(List<Long> list, JsonWriter wrtr) throws Exception {
        for (Long i : list) {
            wrtr.name(i + "").beginObject();
            User objU = userById(i);
            wrtr.name("name").value(objU.getFio())
                .name("login").value(objU.getLogin());
            if (!ObjectUtils.isEmpty(objU.getCode())) {
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
            if (!ObjectUtils.isEmpty(objI.getCode())) objO.addProperty("link", objI.getCode());
            obj.add(i + "", objO);
        }
    }

    public void invitesByList(List<Long> list, JsonWriter wrtr) throws Exception {
        for (Long i : list) {
            wrtr.name(i + "").beginObject();
            Invite objI = inviteById(i);
            wrtr.name("name").value(objI.getFio());
            if (!ObjectUtils.isEmpty(objI.getCode())) {
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

    public void teachersBySchool(School school, JsonObject obj){
        JsonObject nt = new JsonObject(),
            tea = new JsonObject();
        obj.add("nt", nt);
        nt.add("tea", tea);
        if (!ObjectUtils.isEmpty(school.getTeachers())) {
            usersByList(school.getTeachers(), tea);
        }
        if (!ObjectUtils.isEmpty(school.getTeachersInv())) {
            invitesByList(school.getTeachersInv(), tea);
        }
        if (!ObjectUtils.isEmpty(school.getSubjects())) {
            for (Long i1 : school.getSubjects()) {
                Subject subject = subjectById(i1);
                if (subject != null) {
                    JsonObject nt1 = new JsonObject();
                    JsonObject tea1 = new JsonObject();
                    obj.add(i1 + "", nt1);
                    nt1.addProperty("name", subject.getName());
                    nt1.add("tea", tea1);
                    if (!ObjectUtils.isEmpty(subject.getTeachers())) {
                        usersByList(subject.getTeachers(), tea1);
                    }
                    if (!ObjectUtils.isEmpty(subject.getTeachersInv())) {
                        invitesByList(subject.getTeachersInv(), tea1);
                    }
                }
            }
        }
    }

    public void teachersBySchool(School school, JsonWriter wrtr) throws Exception {
        wrtr.name("nt").beginObject().name("tea").beginObject();
        if (!ObjectUtils.isEmpty(school.getTeachers())) {
            usersByList(school.getTeachers(), wrtr);
        }
        if (!ObjectUtils.isEmpty(school.getTeachersInv())) {
            invitesByList(school.getTeachersInv(), wrtr);
        }
        wrtr.endObject().endObject();
        if (!ObjectUtils.isEmpty(school.getSubjects())) {
            for (Long i1 : school.getSubjects()) {
                Subject subject = subjectById(i1);
                if (subject != null) {
                    wrtr.name(i1 + "").beginObject()
                        .name("name").value(subject.getName())
                        .name("tea").beginObject();
                    if (!ObjectUtils.isEmpty(subject.getTeachers())) {
                        usersByList(subject.getTeachers(), wrtr);
                    }
                    if (!ObjectUtils.isEmpty(subject.getTeachersInv())) {
                        invitesByList(subject.getTeachersInv(), wrtr);
                    }
                    wrtr.endObject().endObject();
                }
            }
        }
        wrtr.endObject();
    }

    public void createSubject(Subject subject) {
        Subject savedSubject = subjectRepository.saveAndFlush(subject);
        System.out.println(savedSubject);
    }

    public List<Subject> getSubjects() {
        return subjectRepository.findAll();
    }

    public Subject subjectById(Long id){
        return id == null ? null : subjectRepository.findById(id).orElse(null);
    }

    public Subject subjectByNameAndSchool(String name, Long school){
//        List<Subject> list = subjectRepository.findByNameAndSchool(name, school);
        for(Subject subject : subjectRepository.findBySchool(school)){
            if(Objects.equals(subject.getName(), name)) return subject;
        }
        return null;
    }

    public void createDayOfWeek(DayOfWeek dayOfWeek) {
        DayOfWeek savedDayOfWeek = dayOfWeekRepository.saveAndFlush(dayOfWeek);
        System.out.println(savedDayOfWeek);
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return dayOfWeekRepository.findAll();
    }

    public DayOfWeek dayOfWeekById(Long id){
        return id == null ? null : dayOfWeekRepository.findById(id).orElse(null);
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

}
