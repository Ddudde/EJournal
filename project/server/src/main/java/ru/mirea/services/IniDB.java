package ru.mirea.services;

import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
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

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

public class IniDB {

    private final ServerService serv;

    public IniDB(ServerService serverService) {
        serv = serverService;
        serv.createUser(new User("nm1", "1111",
            "Петров В.В.", Map.of(
            0L, new Role("ex@ya.ru", 5L, 17L, new ArrayList<>(asList(1L, 2L))),
            1L, new Role("ex@ya.ru", 5L, new ArrayList<>(asList(1L, 2L))),
            2L, new Role("ex@ya.ru", new ArrayList<>(), 5L),
            3L, new Role("ex@ya.ru", 5L),
            4L, new Role("ex@ya.ru")
        ), 4L, 1L, 68L));
        serv.createUser(new User("nm12", "1111",
            "Петров В.В.", Map.of(
            0L, new Role("ex@ya.ru", 6L, 17L, new ArrayList<>(asList(1L, 2L))),
            1L, new Role("ex@ya.ru", 6L, new ArrayList<>(asList(1L, 2L))),
            2L, new Role("ex@ya.ru", new ArrayList<>(), 6L),
            3L, new Role("ex@ya.ru", 6L),
            4L, new Role("ex@ya.ru")
        ), 4L, 1L, 69L));
        System.out.println(serv.getUsers());

        serv.createReq(new Request("ex@ya.ru","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
        System.out.println(serv.getRequests());

        serv.createSchool(new School("Школа", asList(7L, 8L), asList(14L), 15L, asList(17L, 18L, 19L, 20L), asList(64L), asList(86L, 87L, 88L)));
        serv.createSchool(new School("Гимназия", asList(1L),asList(9L), asList(14L), 15L, asList(17L, 18L, 19L, 20L), asList(), asList()));
        serv.createSchool(new School("Лицей", asList(2L), asList(14L), 15L, asList(17L, 18L, 19L, 20L), asList(), asList()));
        System.out.println(serv.getSchools());

        serv.createUser(new User("nm13", "1111",
            "Петров В.В.", Map.of(
            3L, new Role("ex@ya.ru", 4L)
        ), 3L, 70L));
        serv.createUser(new User("nm14", "1111",
            "Петров В.В.", Map.of(
            3L, new Role("ex@ya.ru", 4L)
        ), 3L, 71L));

        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        serv.createInvite(new Invite("Петров А.А.", Map.of(
            3L, new Role(null, 5L)
        ), Main.df.format(dateAfter), UUID.randomUUID().toString()));
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
            "Петров В.В.", Map.of(
            0L, new Role("ex@ya.ru", 4L, 17L, new ArrayList<>(asList(1L, 2L)))
        ), 0L, 72L));//16L

        createGroups();//60L
        System.out.println(serv.getGroups());

        serv.createUser(new User("nm16", "1111",
            "Петров В.В.", Map.of(
            0L, new Role("ex@ya.ru", 4L, 18L, new ArrayList<>(asList(62L, 63L)))
        ), 0L, 73L));//61L

        serv.createUser(new User("nm17", "1111",
            "Петров В.В.", Map.of(
            1L, new Role("ex@ya.ru", 4L, new ArrayList<>(asList(61L, 67L)))
        ), 1L, 61L, 74L));//62L

        serv.createUser(new User("nm18", "1111",
            "Петрова В.В.", Map.of(
            1L, new Role("ex@ya.ru", 4L, new ArrayList<>(asList(61L, 67L)))
        ), 1L, 61L, 75L));//63L

        serv.createUser(new User("nm19", "1111",
            "Петрова В1.В.", Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(), 4L)
        ), 2L, 76L));//64L

        serv.createUser(new User("nm20", "1111",
            "Петрова В2.В.", Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(asList("Англ. Яз.")), 4L)
        ), 2L, 77L));//65L

        serv.createUser(new User("nm21", "1111",
            "Петрова В3.В.", Map.of(
            2L, new Role("ex@ya.ru", new ArrayList<>(asList("Математика")), 4L)
        ), 2L, 78L));//66L

        serv.createUser(new User("nm22", "1111",
            "Петров В.Вa.", Map.of(
            0L, new Role("ex@ya.ru", 4L, 18L, new ArrayList<>(asList(62L, 63L)))
        ), 0L, 79L));//67L

        serv.createSettingUser(new SettingUser(2));//68L
        serv.createSettingUser(new SettingUser(1));//69L
        serv.createSettingUser(new SettingUser(2, true, Set.of("4News", "news")));//70L
        serv.createSettingUser(new SettingUser(2));//71L
        serv.createSettingUser(new SettingUser(2));//72L
        serv.createSettingUser(new SettingUser(2, true, Set.of("4News", "news")));//73L
        serv.createSettingUser(new SettingUser(2));//74L
        serv.createSettingUser(new SettingUser(2));//75L
        serv.createSettingUser(new SettingUser(2));//76L
        serv.createSettingUser(new SettingUser(2));//77L
        serv.createSettingUser(new SettingUser(2));//78L
        serv.createSettingUser(new SettingUser(2));//79L

        serv.createLesson(new Lesson(4L, 65L, 18L, 0, 0, "300", "Англ. Яз"));//80L
        serv.createLesson(new Lesson(4L, 65L, 18L, 0, 1, "301", "Англ. Яз"));
        serv.createLesson(new Lesson(4L, 66L, 18L, 0, 2, "302", "Математика"));
        serv.createLesson(new Lesson(4L, 66L, 18L, 1, 0, "303", "Математика"));
        serv.createLesson(new Lesson(4L, 66L, 18L, 1, 5, "32", "Математика"));
        serv.createLesson(new Lesson(4L, 65L, 18L, 1, 6, "362", "Математика"));//85L

        serv.createPeriod(new Period("I четверть", "01.09.22", "03.11.22"));//86L
        serv.createPeriod(new Period("II четверть", "12.11.22", "29.12.22"));//87L
        serv.createPeriod(new Period("IV четверть", "01.04.23", "30.05.23"));//88L
        serv.createMark(new Mark(61L, 88L, 5, 1, "norm", "Ответ на уроке"));//89L
        serv.createMark(new Mark(61L, 88L, 3, 5, "norm", "Контрольная работа"));//90L
        serv.createMark(new Mark(67L, 88L, 4, 5, "norm", "Контрольная работа"));//91L
        serv.createMark(new Mark(61L, 88L, 0, "avg", 4.0f));//92L
        serv.createMark(new Mark(67L, 88L, 0, "avg", 4.0f));//93L
        serv.createDay(new Day(4L, 66L, 18L, "Математика", null, "05.06.23", new ArrayList<>(asList(89L, 91L))));
        serv.createDay(new Day(4L, 66L, 18L, "Математика", null, "06.06.23", new ArrayList<>(asList(90L))));
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
            School school = serv.schoolById(serv.getFirstRole(inv.getRoles()).getYO());
            school.getHteachersInv().remove(inv.getId());
            serv.getSchoolRepository().saveAndFlush(school);
            serv.getInviteRepository().delete(inv);
        }
    }

    public void createGroups(){
        serv.createGroup(new Group("11A", new ArrayList<>(asList(1L, 2L, 16L))));//17L
        serv.createGroup(new Group("11Б", new ArrayList<>(asList(61L, 67L))));
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
