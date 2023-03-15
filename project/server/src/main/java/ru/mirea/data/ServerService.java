package ru.mirea.data;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import ru.mirea.Main;
import ru.mirea.data.json.Role;
import ru.mirea.data.models.*;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.reps.*;
import ru.mirea.data.reps.auth.InviteRepository;
import ru.mirea.data.reps.auth.UserRepository;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

@Getter
@Service public class ServerService {

    private final UserRepository userRepository;

    private final InviteRepository inviteRepository;

    private final SchoolRepository schoolRepository;

    private final RequestRepository requestRepository;

    private final SystemRepository systemRepository;

    private final NewsRepository newsRepository;

    private final ContactsRepository contactsRepository;

    private final GroupRepository groupRepository;

    public ServerService(UserRepository userRepository, InviteRepository inviteRepository, SchoolRepository schoolRepository, RequestRepository requestRepository, SystemRepository systemRepository, NewsRepository newsRepository, ContactsRepository contactsRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.schoolRepository = schoolRepository;
        this.requestRepository = requestRepository;
        this.systemRepository = systemRepository;
        this.newsRepository = newsRepository;
        this.contactsRepository = contactsRepository;
        this.groupRepository = groupRepository;

        createUser(new User("nm1", "1111", "Петров В.В.", 2, new HashMap<Long,Role>() {{
            put(0L, new Role("ex@ya.ru", 5L, 17L, new ArrayList<>(asList(1L, 2L))));
            put(1L, new Role("ex@ya.ru", 5L, new ArrayList<>(asList(1L, 2L))));
            put(2L, new Role("ex@ya.ru", new ArrayList<>(asList("Англ. Яз.", "Математика")), 5L));
            put(3L, new Role("ex@ya.ru", 5L));
            put(4L, new Role("ex@ya.ru"));
        }}));
        createUser(new User("nm12", "1111", "Петров В.В.", 1, new HashMap<Long,Role>() {{
            put(0L, new Role("ex@ya.ru", 6L, 17L, new ArrayList<>(asList(1L, 2L))));
            put(1L, new Role("ex@ya.ru", 6L, new ArrayList<>(asList(1L, 2L))));
            put(2L, new Role("ex@ya.ru", new ArrayList<>(asList("Англ. Яз.", "Математика")), 6L));
            put(3L, new Role("ex@ya.ru", 6L));
            put(4L, new Role("ex@ya.ru"));
        }}));
        System.out.println(getUsers());

        createReq(new Request("ex@ya.ru","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
        System.out.println(getRequests());

        createSchool(new School("Школа", new ArrayList<>(asList(7L, 8L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L))));
        createSchool(new School("Гимназия", new ArrayList<>(asList(1L)), new ArrayList<>(asList(9L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L))));
        createSchool(new School("Лицей", new ArrayList<>(asList(2L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L))));
        System.out.println(getSchools());

        createUser(new User("nm13", "1111", "Петров В.В.", 2, new HashMap<Long,Role>() {{
            put(3L, new Role("ex@ya.ru", 4L));
        }}));
        createUser(new User("nm14", "1111", "Петров В.В.", 2, new HashMap<Long,Role>() {{
            put(3L, new Role("ex@ya.ru", 4L));
        }}));

        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        createInvite(new Invite("Петров А.А.", new HashMap<>(){{
            put(3L, new Role(null, 5L));
        }}, Main.df.format(dateAfter)));
        System.out.println(getInvites());
        checkDates();
        System.out.println(getInvites());

        createSyst(new Syst(new ArrayList<>(asList(1L, 2L)), new ArrayList<>(asList(11L, 12L)), 13L));
        System.out.println(getSyst());

        createNews(new News("День рождения портала!","25.04.2022", "Начались первые работы"));
        createNews(new News("А проект вышел большим...","02.12.2022", "/media/tuman.jpg", "Да-да, всё ещё не конец..."));
        System.out.println(getNews());

        createContacts(new Contacts(
                "8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/media/map.jpg"));
        System.out.println(getContacts());

        createNews(new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));

        createContacts(new Contacts(
                "8 (800) 555 35 36\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/media/map.jpg"));

        createUser(new User("nm15", "1111", "Петров В.В.", 2, new HashMap<Long,Role>() {{
            put(0L, new Role("ex@ya.ru", 4L, 17L, new ArrayList<>(asList(1L, 2L))));
        }}));//16L

        createGroups();//60L
        System.out.println(getGroups());

        createUser(new User("nm16", "1111", "Петров В.В.", 2, new HashMap<Long,Role>() {{
            put(0L, new Role("ex@ya.ru", 4L, 18L, new ArrayList<>(asList(1L, 2L))));
        }}));//61L
    }

    private void checkDates(){
        try {
            long now = Main.df.parse(Main.df.format(new Date())).getTime();
            for(Invite inv : getInvites()){
                if(now >= Main.df.parse(inv.getExpDate()).getTime()){
                    delInv(inv);
                    System.out.println("Удалён код " + inv.getCode() + " по истечению срока действия");
                }
            }
            for(User user : getUsers()){
                if(!ObjectUtils.isEmpty(user.getExpDate()) && now >= Main.df.parse(user.getExpDate()).getTime()){
                    delCodeUser(user);
                    System.out.println("Удалён код " + user.getCode() + " по истечению срока действия");
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void createUser(User user) {
        User savedUser = userRepository.saveAndFlush(user);
        System.out.println(savedUser);
    }

    public void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            userRepository.saveAndFlush(user);
        }
    }

    public List<User> getUsers() {
        return userRepository.findAll();
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

    public void delInv(Invite inv) {
        if(inv != null){
            School school = schoolById(getFirstRole(inv.getRole()).getYO());
            if(ObjectUtils.isEmpty(school.getHteachersInv())) {
                school.setHteachersInv(new ArrayList<>());
            }
            school.getHteachersInv().remove(inv.getId());
            schoolRepository.saveAndFlush(school);
            inviteRepository.delete(inv);
        }
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
        Syst savedSyst = systemRepository.saveAndFlush(syst);
        System.out.println(savedSyst);
    }

    public Syst getSyst() {
        List<Syst> systs = systemRepository.findAll();
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

    public void createGroups(){
        createGroup(new Group("11A", new ArrayList<>(asList(1L, 2L, 16L))));//17L
        createGroup(new Group("11Б", new ArrayList<>(asList(61L))));
        createGroup(new Group("11В"));
        createGroup(new Group("11Г"));
        createGroup(new Group("10А"));
        createGroup(new Group("10Б"));
        createGroup(new Group("10В"));
        createGroup(new Group("10Г"));
        createGroup(new Group("9А"));
        createGroup(new Group("9Б"));
        createGroup(new Group("9В"));
        createGroup(new Group("9Г"));
        createGroup(new Group("8А"));
        createGroup(new Group("8Б"));
        createGroup(new Group("8В"));
        createGroup(new Group("8Г"));
        createGroup(new Group("7А"));
        createGroup(new Group("7Б"));
        createGroup(new Group("7В"));
        createGroup(new Group("7Г"));
        createGroup(new Group("6А"));
        createGroup(new Group("6Б"));
        createGroup(new Group("6В"));
        createGroup(new Group("6Г"));
        createGroup(new Group("5А"));
        createGroup(new Group("5Б"));
        createGroup(new Group("5В"));
        createGroup(new Group("5Г"));
        createGroup(new Group("4А"));
        createGroup(new Group("4Б"));
        createGroup(new Group("4В"));
        createGroup(new Group("4Г"));
        createGroup(new Group("3А"));
        createGroup(new Group("3Б"));
        createGroup(new Group("3В"));
        createGroup(new Group("3Г"));
        createGroup(new Group("2А"));
        createGroup(new Group("2Б"));
        createGroup(new Group("2В"));
        createGroup(new Group("2Г"));
        createGroup(new Group("1А"));
        createGroup(new Group("1Б"));
        createGroup(new Group("1В"));
        createGroup(new Group("1Г"));//60L
    }
}
