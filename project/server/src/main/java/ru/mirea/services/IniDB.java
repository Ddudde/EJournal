package ru.mirea.services;

import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
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

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class IniDB {

    private final ServerService serv;

    public IniDB(ServerService serverService) {
        serv = serverService;
        serv.createUser(new User("nm1", "1111",
            "Петров В.В.", 2, Map.of(
            0L, new Role("ex@ya.ru", 5L, 17L, new ArrayList<>(asList(1L, 2L))),
            1L, new Role("ex@ya.ru", 5L, new ArrayList<>(asList(1L, 2L))),
            2L, new Role("ex@ya.ru", new ArrayList<>(), 5L),
            3L, new Role("ex@ya.ru", 5L),
            4L, new Role("ex@ya.ru")
            ), 4L, 1L));
        serv.createUser(new User("nm12", "1111",
            "Петров В.В.", 1, Map.of(
            0L, new Role("ex@ya.ru", 6L, 17L, new ArrayList<>(asList(1L, 2L))),
            1L, new Role("ex@ya.ru", 6L, new ArrayList<>(asList(1L, 2L))),
            2L, new Role("ex@ya.ru", new ArrayList<>(), 6L),
            3L, new Role("ex@ya.ru", 6L),
            4L, new Role("ex@ya.ru")
            ), 4L, 1L));
        System.out.println(serv.getUsers());

        serv.createReq(new Request("ex@ya.ru","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
        System.out.println(serv.getRequests());

        serv.createSchool(new School("Школа", new ArrayList<>(asList(7L, 8L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L)), new ArrayList<>(asList(64L, 65L)), new ArrayList<>(asList(66L))));
        serv.createSchool(new School("Гимназия", new ArrayList<>(asList(1L)), new ArrayList<>(asList(9L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L)), new ArrayList<>(), new ArrayList<>()));
        serv.createSchool(new School("Лицей", new ArrayList<>(asList(2L)), new ArrayList<>(asList(14L)), 15L, new ArrayList<>(asList(17L, 18L, 19L, 20L)), new ArrayList<>(), new ArrayList<>()));
        System.out.println(serv.getSchools());

        serv.createUser(new User("nm13", "1111",
            "Петров В.В.", 2, Map.of(
            3L, new Role("ex@ya.ru", 4L)
            ), 3L, null, Set.of("4_news")));
        serv.createUser(new User("nm14", "1111",
            "Петров В.В.", 2, Map.of(
            3L, new Role("ex@ya.ru", 4L)
            ), 3L));

        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        serv.createInvite(new Invite("Петров А.А.", Map.of(
            3L, new Role(null, 5L)
            ), Main.df.format(dateAfter)));
        System.out.println(serv.getInvites());
        checkDates();
        System.out.println(serv.getInvites());

        serv.createSyst(new Syst(new ArrayList<>(asList(1L, 2L)), new ArrayList<>(asList(11L, 12L)), 13L));
        System.out.println(serv.getSyst());

        serv.createNews(new News("День рождения портала!","25.04.2022", "Начались первые работы"));
        serv.createNews(new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец..."));
        System.out.println(serv.getNews());

        serv.createContacts(new Contacts(
            "8 (800) 555 35 37\n5 (353) 555 00 88",
            "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
            "/static/media/map.jpg"));
        System.out.println(serv.getContacts());

        serv.createNews(new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));

        serv.createContacts(new Contacts(
            "8 (800) 555 35 36\n5 (353) 555 00 88",
            "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
            "/static/media/map.jpg"));

        serv.createUser(new User("nm15", "1111",
            "Петров В.В.", 2, Map.of(
            0L, new Role("ex@ya.ru", 4L, 17L, new ArrayList<>(asList(1L, 2L)))
            ), 0L));//16L

        createGroups();//60L
        System.out.println(serv.getGroups());

        serv.createUser(new User("nm16", "1111",
            "Петров В.В.", 2, Map.of(
            0L, new Role("ex@ya.ru", 4L, 18L, new ArrayList<>(asList(62L, 63L)))
            ), 0L));//61L

        serv.createUser(new User("nm17", "1111",
            "Петров В.В.", 2, Map.of(
            1L, new Role("ex@ya.ru", 4L, new ArrayList<>(asList(61L, 75L)))
            ), 1L, 61L));//62L

        serv.createUser(new User("nm18", "1111",
            "Петрова В.В.", 2, Map.of(
            1L, new Role("ex@ya.ru", 4L, new ArrayList<>(asList(61L, 75L)))
            ), 1L, 61L));//63L

        serv.createSubject(new Subject("Англ. Яз.", 4L, new ArrayList<>(asList(67L))));
        serv.createSubject(new Subject("Математика", 4L, new ArrayList<>(asList(68L))));
        System.out.println(serv.getSubjects());

        serv.createUser(new User("nm19", "1111",
            "Петрова В1.В.", 2, Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(), 4L)
            ), 2L));//66L

        serv.createUser(new User("nm20", "1111",
            "Петрова В2.В.", 2, Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(asList(64L)), 4L)
            ), 2L));//67L

        serv.createUser(new User("nm21", "1111",
            "Петрова В3.В.", 2, Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(asList(65L)), 4L)
            ), 2L));//68L

        serv.createLesson(new Lesson(64L, 67L, "300"));
        serv.createLesson(new Lesson(64L, 67L, "301"));
        serv.createLesson(new Lesson(65L, 68L, "302"));
        serv.createLesson(new Lesson(65L, 68L, "303"));
        System.out.println(serv.getLessons());
        serv.createDayOfWeek(new DayOfWeek(Map.of(0L, 69L, 1L, 70L, 2L, 71L)));
        serv.createDayOfWeek(new DayOfWeek(Map.of(0L, 72L)));
        System.out.println(serv.getDaysOfWeek());

        serv.createUser(new User("nm22", "1111",
            "Петров В.Вa.", 2, Map.of(
            0L, new Role("ex@ya.ru", 4L, 18L, new ArrayList<>(asList(62L, 63L)))
            ), 0L));//75L
    }

    private void checkDates(){
        try {
            long now = Main.df.parse(Main.df.format(new Date())).getTime();
            for(Invite inv : serv.getInvites()){
                if(now >= Main.df.parse(inv.getExpDate()).getTime()){
                    delInv(inv);
                    System.out.println("Удалён код " + inv.getCode() + " по истечению срока действия");
                }
            }
            for(User user : serv.getUsers()){
                if(!ObjectUtils.isEmpty(user.getExpDate()) && now >= Main.df.parse(user.getExpDate()).getTime()){
                    delCodeUser(user);
                    System.out.println("Удалён код " + user.getCode() + " по истечению срока действия");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            serv.getUserRepository().saveAndFlush(user);
        }
    }

    public void delInv(Invite inv) {
        if(inv != null){
            School school = serv.schoolById(serv.getFirstRole(inv.getRole()).getYO());
            school.getHteachersInv().remove(inv.getId());
            serv.getSchoolRepository().saveAndFlush(school);
            serv.getInviteRepository().delete(inv);
        }
    }

    public void createGroups(){
        serv.createGroup(new Group("11A", new ArrayList<>(asList(1L, 2L, 16L))));//17L
        serv.createGroup(new Group("11Б", new ArrayList<>(asList(61L, 75L)), Map.of(0L, 73L, 1L, 74L)));
        serv.createGroup(new Group("11В"));
        serv.createGroup(new Group("11Г"));
        serv.createGroup(new Group("10А"));
        serv.createGroup(new Group("10Б"));
        serv.createGroup(new Group("10В"));
        serv.createGroup(new Group("10Г"));
        serv.createGroup(new Group("9А"));
        serv.createGroup(new Group("9Б"));
        serv.createGroup(new Group("9В"));
        serv.createGroup(new Group("9Г"));
        serv.createGroup(new Group("8А"));
        serv.createGroup(new Group("8Б"));
        serv.createGroup(new Group("8В"));
        serv.createGroup(new Group("8Г"));
        serv.createGroup(new Group("7А"));
        serv.createGroup(new Group("7Б"));
        serv.createGroup(new Group("7В"));
        serv.createGroup(new Group("7Г"));
        serv.createGroup(new Group("6А"));
        serv.createGroup(new Group("6Б"));
        serv.createGroup(new Group("6В"));
        serv.createGroup(new Group("6Г"));
        serv.createGroup(new Group("5А"));
        serv.createGroup(new Group("5Б"));
        serv.createGroup(new Group("5В"));
        serv.createGroup(new Group("5Г"));
        serv.createGroup(new Group("4А"));
        serv.createGroup(new Group("4Б"));
        serv.createGroup(new Group("4В"));
        serv.createGroup(new Group("4Г"));
        serv.createGroup(new Group("3А"));
        serv.createGroup(new Group("3Б"));
        serv.createGroup(new Group("3В"));
        serv.createGroup(new Group("3Г"));
        serv.createGroup(new Group("2А"));
        serv.createGroup(new Group("2Б"));
        serv.createGroup(new Group("2В"));
        serv.createGroup(new Group("2Г"));
        serv.createGroup(new Group("1А"));
        serv.createGroup(new Group("1Б"));
        serv.createGroup(new Group("1В"));
        serv.createGroup(new Group("1Г"));//60L
    }
}
