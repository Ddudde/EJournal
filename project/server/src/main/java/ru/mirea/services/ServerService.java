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
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.data.models.school.day.Day;
import ru.mirea.data.models.school.day.Mark;
import ru.mirea.data.reps.ContactsRepository;
import ru.mirea.data.reps.NewsRepository;
import ru.mirea.data.reps.SystRepository;
import ru.mirea.data.reps.auth.RoleRepository;
import ru.mirea.data.reps.auth.SettingUserRepository;
import ru.mirea.data.reps.auth.UserRepository;
import ru.mirea.data.reps.school.*;
import ru.mirea.data.reps.school.day.DayRepository;
import ru.mirea.data.reps.school.day.MarkRepository;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Getter
@Service public class ServerService {

    private final UserRepository userRepository;

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

    private List<SettingUser> setts;

    private List<News> newsList;

    private List<Contacts> contactsList;

    private List<Period> periods;

    private List<Group> groups;

    private List<School> schools;

    private List<Lesson> lessons;

    private List<Role> roles;

    private List<User> users;

    private Request request;

    private Syst syst;

    private List<Mark> marks;

    private List<Day> days;

    public ServerService(UserRepository userRepository, SchoolRepository schoolRepository, RequestRepository requestRepository, SystRepository systRepository, NewsRepository newsRepository, ContactsRepository contactsRepository, GroupRepository groupRepository, DayRepository dayRepository, LessonRepository lessonRepository, MarkRepository markRepository, SettingUserRepository settingUserRepository, PeriodRepository periodRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
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
                .name("name").value(objU.getFio());
            if (!ObjectUtils.isEmpty(objU.getLogin())) {
                wrtr.name("login").value(objU.getLogin());
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
            if (!ObjectUtils.isEmpty(objU.getLogin())) {
                wrtr.name("login").value(objU.getLogin());
            }
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
        wrtr.endObject().endObject();
        List<Object[]> lessU = getLessonRepository().uniqTeachersBySchool(4L);
        if (!ObjectUtils.isEmpty(lessU)) {
            Map<String, List<Long>> mapU = lessU.stream().collect(Collectors.groupingBy(
                obj -> (String) obj[0],
                Collectors.mapping(obj -> (Long) obj[1], Collectors.toList())
            ));
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

    private void checkDates(){
        try {
            long now = Main.df.parse(Main.df.format(new Date())).getTime();
            for(User user : getUsers()){
                if(!ObjectUtils.isEmpty(user.getExpDate()) && now >= Main.df.parse(user.getExpDate()).getTime()){
                    if(user.getLogin() == null) {
                        delInv(user);
                    } else {
                        delCodeUser(user);
                    }
                    System.out.println("Удалён код " + user.getCode() + " по истечению срока действия");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            getUserRepository().saveAndFlush(user);
        }
    }

    private void delInv(User inv) {
        if(inv != null){
            School school = getFirstRole(inv.getRoles()).getYO();
            school.getHteachers().remove(inv);
            getSchoolRepository().saveAndFlush(school);
            getUserRepository().delete(inv);
        }
    }

    private List<Group> createGroups(){
        return getGroupRepository().saveAllAndFlush(asList(
            new Group("11A"),//17L
            new Group("11Б"),
            new Group("11В"),
            new Group("11Г"),
            new Group("10А"),
            new Group("10Б"),
            new Group("10В"),
            new Group("10Г"),
            new Group("9А"),
            new Group("9Б"),
            new Group("9В"),
            new Group("9Г"),
            new Group("8А"),
            new Group("8Б"),
            new Group("8В"),
            new Group("8Г"),
            new Group("7А"),
            new Group("7Б"),
            new Group("7В"),
            new Group("7Г"),
            new Group("6А"),
            new Group("6Б"),
            new Group("6В"),
            new Group("6Г"),
            new Group("5А"),
            new Group("5Б"),
            new Group("5В"),
            new Group("5Г"),
            new Group("4А"),
            new Group("4Б"),
            new Group("4В"),
            new Group("4Г"),
            new Group("3А"),
            new Group("3Б"),
            new Group("3В"),
            new Group("3Г"),
            new Group("2А"),
            new Group("2Б"),
            new Group("2В"),
            new Group("2Г"),
            new Group("1А"),
            new Group("1Б"),
            new Group("1В"),
            new Group("1Г")//60L
        ));
    }

    public void testOn(){
        setts = getSettingUserRepository().saveAllAndFlush(asList(
            new SettingUser(2),
            new SettingUser(1),
            new SettingUser(2, true, Set.of("4News", "news")),
            new SettingUser(2),
            new SettingUser(2),
            new SettingUser(2, true, Set.of("4News", "news")),
            new SettingUser(2),
            new SettingUser(2),
            new SettingUser(2),
            new SettingUser(2),
            new SettingUser(2),
            new SettingUser(2)
        ));

        newsList = getNewsRepository().saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец..."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте.")
        ));

        contactsList = getContactsRepository().saveAllAndFlush(asList(
            new Contacts(
                "8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg"),
            new Contacts(
                "8 (800) 555 35 36\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg")
        ));

        periods = getPeriodRepository().saveAllAndFlush(asList(
            new Period("I четверть", "01.09.22", "03.11.22"),//86L
            new Period("II четверть", "12.11.22", "29.12.22"),//87L
            new Period("IV четверть", "01.04.23", "30.05.23")//88L
        ));

        groups = createGroups();//60L

        schools = getSchoolRepository().saveAllAndFlush(asList(
            new School("Школа", asList(newsList.get(2)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList(periods.get(0), periods.get(1), periods.get(2))),
            new School("Гимназия", asList(newsList.get(3)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList()),
            new School("Лицей", asList(newsList.get(4)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList())
        ));

        request = getRequestRepository().saveAndFlush(new Request("ex@ya.ru","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
        System.out.println(getRequests());

        lessons = getLessonRepository().saveAllAndFlush(asList(
            new Lesson(schools.get(0), groups.get(1), 0, 0, "300", "Англ. Яз"),//80L
            new Lesson(schools.get(0), groups.get(1), 0, 1, "301", "Англ. Яз"),
            new Lesson(schools.get(0), groups.get(1), 0, 2, "302", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 0, "303", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 5, "32", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 6, "362", "Математика")//85L
        ));

        syst = createSyst(new Syst(asList(newsList.get(0), newsList.get(1)), contactsList.get(0)));
        System.out.println(getSyst());

        roles = getRoleRepository().saveAllAndFlush(asList(
            new Role("ex@ya.ru", Set.of(), schools.get(1)),
            new Role("ex@ya.ru", schools.get(1)),
            new Role("ex@ya.ru"),

            new Role("ex@ya.ru", Set.of(), schools.get(2)),
            new Role("ex@ya.ru", schools.get(2)),
            new Role("ex@ya.ru"),

            new Role("ex@ya.ru", schools.get(0)),

            new Role("ex@ya.ru", schools.get(0)),

            new Role("ex@ya.ru", schools.get(0), groups.get(0)),

            new Role("ex@ya.ru", schools.get(0), groups.get(1)),

            new Role("ex@ya.ru", schools.get(0), groups.get(1)),

            new Role("ex@ya.ru", schools.get(0)),

            new Role("ex@ya.ru", schools.get(0)),

            new Role("ex@ya.ru", Set.of(), schools.get(0)),

            new Role("ex@ya.ru", Set.of("Англ. Яз."), schools.get(0)),

            new Role("ex@ya.ru", Set.of("Математика"), schools.get(0)),

            new Role(null, schools.get(1))//inv
        ));

        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);

        users = getUserRepository().saveAllAndFlush(asList(
            new User("nm1", "1111",
                "Петров В.В.", Map.of(
                2L, roles.get(0),
                3L, roles.get(1),
                4L, roles.get(2)
            ), 4L, 1L, setts.get(0)),
            new User("nm121", "1111",
                "Петров В.В.", Map.of(
                2L, roles.get(3),
                3L, roles.get(4),
                4L, roles.get(5)
            ), 4L, 1L, setts.get(1)),
            new User("nm13", "1111",
                "Петров В.В.", Map.of(
                3L, roles.get(6)
            ), 3L, setts.get(2)),
            new User("nm14", "1111",
                "Петров В.В.", Map.of(
                3L, roles.get(7)
            ), 3L, setts.get(3)),
            new User("nm15", "1111",
                "Петров В.В.", Map.of(
                0L, roles.get(8)
            ), 0L, setts.get(4)),//16L
            new User("nm16", "1111",
                "Петров В.В.", Map.of(
                0L, roles.get(9)
            ), 0L, setts.get(5)),//61L
            new User("nm22", "1111",
                "Петров В.Вa.", Map.of(
                0L, roles.get(10)
            ), 0L, setts.get(11)),//67L
            new User("nm17", "1111",
                "Петров В.В.", Map.of(
                1L, roles.get(11)
            ), 1L, 61L, setts.get(6)),//62L
            new User("nm18", "1111",
                "Петрова В.В.", Map.of(
                1L, roles.get(12)
            ), 1L, 61L, setts.get(7)),//63L
            new User("nm19", "1111",
                "Петрова В1.В.", Map.of(
                2L, roles.get(13)
            ), 2L, setts.get(8)),//64L
            new User("nm20", "1111",
                "Петрова В2.В.", Map.of(
                2L, roles.get(14)
            ), 2L, setts.get(9)),//65L
            new User("nm21", "1111",
                "Петрова В3.В.", Map.of(
                2L, roles.get(15)
            ), 2L, setts.get(10)),//66L
            new User("Петров А.А.", Map.of(
                3L, roles.get(16)
            ), Main.df.format(dateAfter), UUID.randomUUID().toString())//inv
        ));

        groups.get(0).getKids().add(users.get(4));
        groups.get(1).getKids().addAll(users.subList(5, 7));
        groups = getGroupRepository().saveAllAndFlush(groups);

        users.get(5).getRole(0L).getParents().addAll(users.subList(7, 9));
        users.get(6).getRole(0L).getParents().addAll(users.subList(7, 9));
        users.get(7).getRole(1L).getKids().addAll(users.subList(5, 7));
        users.get(8).getRole(1L).getKids().addAll(users.subList(5, 7));
        users = getUserRepository().saveAllAndFlush(users);

        schools.get(0).getHteachers().addAll(users.subList(2, 4));
        schools.get(0).getTeachers().addAll(users.subList(9, 12));
        schools.get(1).getHteachers().addAll(asList(users.get(0), users.get(12)));
        schools.get(2).getHteachers().add(users.get(1));
        schools = getSchoolRepository().saveAllAndFlush(schools);

        lessons.get(0).setTeacher(users.get(10));
        lessons.get(1).setTeacher(users.get(10));
        lessons.get(2).setTeacher(users.get(11));
        lessons.get(3).setTeacher(users.get(11));
        lessons.get(4).setTeacher(users.get(11));
        lessons.get(5).setTeacher(users.get(10));
        lessons = getLessonRepository().saveAllAndFlush(lessons);

        syst.getAdmins().addAll(users.subList(0, 2));
        syst = getSystRepository().saveAndFlush(syst);

        checkDates();

        marks = getMarkRepository().saveAllAndFlush(asList(
            new Mark(users.get(5), periods.get(2), 5, 1, "norm", "Ответ на уроке"),//89L
            new Mark(users.get(5), periods.get(2), 3, 5, "norm", "Контрольная работа"),//90L
            new Mark(users.get(6), periods.get(2), 4, 5, "norm", "Контрольная работа"),//91L
            new Mark(users.get(5), periods.get(2), 0, "avg", 4.0f),//92L
            new Mark(users.get(6), periods.get(2), 0, "avg", 4.0f)//93L
        ));

        days = getDayRepository().saveAllAndFlush(asList(
            new Day(schools.get(0), users.get(11), groups.get(1), "Математика", null, "05.06.23", asList(marks.get(0), marks.get(2))),
            new Day(schools.get(0), users.get(11), groups.get(1), "Математика", null, "06.06.23", asList(marks.get(1)))
        ));
    }

    public void testOff(){
        dayRepository.deleteAll(days);
        days.clear();
        markRepository.deleteAll(marks);
        marks.clear();
        systRepository.delete(syst);
        syst = null;
        lessonRepository.deleteAll(lessons);
        lessons.clear();
        for(User user : users) {
            user.rem();
        }
        users = userRepository.saveAllAndFlush(users);
        roleRepository.deleteAll(roles);
        roles.clear();
        schoolRepository.deleteAll(schools);
        schools.clear();
        groupRepository.deleteAll(groups);
        groups.clear();
        userRepository.deleteAll(users);
        users.clear();
        requestRepository.delete(request);
        request = null;
        periodRepository.deleteAll(periods);
        periods.clear();
        contactsRepository.deleteAll(contactsList);
        contactsList.clear();
        newsRepository.deleteAll(newsList);
        newsList.clear();
        settingUserRepository.deleteAll(setts);
        setts.clear();
    }

    private void getUsersT(JsonTreeWriter wrtr, List<User> users) throws Exception {
        for (User user : users) {
            wrtr.name(user.getId()+"").beginObject()
                .name("fio").value(user.getFio())
                .name("login").value(user.getLogin())
                .name("pass").value(user.getPassword())
                .endObject();
        }
    }

    public void getTestInfo(JsonTreeWriter wrtr) throws Exception {
        wrtr.name("bodyT").beginObject()
            .name("admins").beginObject();
        if(syst != null) getUsersT(wrtr, syst.getAdmins());
        wrtr.endObject()
            .name("schools").beginObject();
        if(schools != null) {
            for (School school : schools) {
                wrtr.name(school.getId()+"").beginObject()
                    .name("name").value(school.getName())
                    .name("hteachers").beginObject();
                getUsersT(wrtr, school.getHteachers());
                wrtr.endObject()
                    .name("teachers").beginObject();
                getUsersT(wrtr, school.getTeachers());
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
}