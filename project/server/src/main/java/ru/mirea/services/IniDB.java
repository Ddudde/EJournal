package ru.mirea.services;

import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.Role;
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
        List<SettingUser> setts = serv.getSettingUserRepository().saveAllAndFlush(asList(
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

        List<News> newsList = serv.getNewsRepository().saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец..."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."),
            new News("Мы перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте.")
        ));

        List<Contacts> contactsList = serv.getContactsRepository().saveAllAndFlush(asList(
            new Contacts(
                "8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg"),
            new Contacts(
                "8 (800) 555 35 36\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg")
        ));

        List<Period> periods = serv.getPeriodRepository().saveAllAndFlush(asList(
            new Period("I четверть", "01.09.22", "03.11.22"),//86L
            new Period("II четверть", "12.11.22", "29.12.22"),//87L
            new Period("IV четверть", "01.04.23", "30.05.23")//88L
        ));

        List<Group> groups = createGroups();//60L

        List<School> schools = serv.getSchoolRepository().saveAllAndFlush(asList(
            new School("Школа", asList(newsList.get(2)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList(periods.get(0), periods.get(1), periods.get(2))),
            new School("Гимназия", asList(newsList.get(3)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList()),
            new School("Лицей", asList(newsList.get(4)), contactsList.get(1), asList(groups.get(0), groups.get(1), groups.get(2), groups.get(3)), asList())
        ));

        serv.createReq(new Request("ex@ya.ru","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
        System.out.println(serv.getRequests());

        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        Role role2 = serv.getRoleRepository().saveAndFlush(new Role(null, schools.get(1)));
        Invite invite = serv.createInvite(new Invite("Петров А.А.", Map.of(
            3L, role2
        ), Main.df.format(dateAfter), UUID.randomUUID().toString()));
        System.out.println(serv.getInvites());
        checkDates();
        System.out.println(serv.getInvites());

        List<Lesson> lessons = serv.getLessonRepository().saveAllAndFlush(asList(
            new Lesson(schools.get(0), groups.get(1), 0, 0, "300", "Англ. Яз"),//80L
            new Lesson(schools.get(0), groups.get(1), 0, 1, "301", "Англ. Яз"),
            new Lesson(schools.get(0), groups.get(1), 0, 2, "302", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 0, "303", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 5, "32", "Математика"),
            new Lesson(schools.get(0), groups.get(1), 1, 6, "362", "Математика")//85L
        ));

        Syst syst = serv.createSyst(new Syst(asList(newsList.get(0), newsList.get(1)), contactsList.get(0)));
        System.out.println(serv.getSyst());

        List<Role> roles = serv.getRoleRepository().saveAllAndFlush(asList(
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

            new Role("ex@ya.ru", Set.of("Математика"), schools.get(0))
        ));

        List<User> users = serv.getUserRepository().saveAllAndFlush(asList(
            new User("nm1", "1111",
                "Петров В.В.", Map.of(
                2L, roles.get(0),
                3L, roles.get(1),
                4L, roles.get(2)
            ), 4L, 1L, setts.get(0)),
            new User("nm12", "1111",
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
            ), 2L, setts.get(10))//66L
        ));

        groups.get(0).getKids().add(users.get(4));
        groups.get(1).getKids().addAll(users.subList(5, 7));
        serv.getGroupRepository().saveAllAndFlush(groups.subList(0, 2));

        users.get(5).getRole(0L).getParents().addAll(users.subList(7, 9));
        users.get(6).getRole(0L).getParents().addAll(users.subList(7, 9));
        users.get(7).getRole(1L).getKids().addAll(users.subList(5, 7));
        users.get(8).getRole(1L).getKids().addAll(users.subList(5, 7));
        serv.getUserRepository().saveAllAndFlush(users.subList(5, 9));

        schools.get(0).getHteachers().addAll(users.subList(2, 4));
        schools.get(0).getTeachers().addAll(users.subList(9, 12));
        schools.get(1).getHteachers().add(users.get(0));
        schools.get(1).getHteachersInv().add(invite);
        schools.get(2).getHteachers().add(users.get(1));
        serv.getSchoolRepository().saveAllAndFlush(schools);

        lessons.get(0).setTeacher(users.get(10));
        lessons.get(1).setTeacher(users.get(10));
        lessons.get(2).setTeacher(users.get(11));
        lessons.get(3).setTeacher(users.get(11));
        lessons.get(4).setTeacher(users.get(11));
        lessons.get(5).setTeacher(users.get(10));
        serv.getLessonRepository().saveAllAndFlush(lessons);

        syst.getAdmins().addAll(users.subList(0, 2));
        serv.getSystRepository().saveAndFlush(syst);

        List<Mark> marks = serv.getMarkRepository().saveAllAndFlush(asList(
            new Mark(users.get(5), periods.get(2), 5, 1, "norm", "Ответ на уроке"),//89L
            new Mark(users.get(5), periods.get(2), 3, 5, "norm", "Контрольная работа"),//90L
            new Mark(users.get(6), periods.get(2), 4, 5, "norm", "Контрольная работа"),//91L
            new Mark(users.get(5), periods.get(2), 0, "avg", 4.0f),//92L
            new Mark(users.get(6), periods.get(2), 0, "avg", 4.0f)//93L
        ));

        serv.createDay(new Day(schools.get(0), users.get(11), groups.get(1), "Математика", null, "05.06.23", asList(marks.get(0), marks.get(2))));
        serv.createDay(new Day(schools.get(0), users.get(11), groups.get(1), "Математика", null, "06.06.23", asList(marks.get(1))));
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
            School school = serv.getFirstRole(inv.getRoles()).getYO();
            school.getHteachersInv().remove(inv);
            serv.getSchoolRepository().saveAndFlush(school);
            serv.getInviteRepository().delete(inv);
        }
    }

    public List<Group> createGroups(){
        return serv.getGroupRepository().saveAllAndFlush(asList(
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
}
