package ru.mirea.services.db;

import com.github.javafaker.Faker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.mirea.Main;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.Syst;
import ru.mirea.data.models.auth.Role;
import ru.mirea.data.models.auth.SettingUser;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.*;
import ru.mirea.data.models.school.Day;
import ru.mirea.data.models.school.Mark;
import ru.mirea.services.ServerService;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

@Getter
@NoArgsConstructor
@Service public class IniDBService {

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

    private final Faker fakerRu = new Faker(new Locale("ru"));

    private final Faker fakerEn = new Faker();

    private final String[] namesSch = {"Гимназия №", "Школа №", "Лицей №"};

    private final String[] namesGrp = {"А", "Б", "В", "Г", "Д"};

    private final String[] namesSubj = {"Англ. Яз.", "Математика", "Русский Яз.", "Химия", "Физика"};

    private Date dateAfter;

    private ServerService serv;

    public void firstIni(ServerService serv) {
        this.serv = serv;
        SettingUser setts = serv.getDbService().getSettingUserRepository().saveAndFlush(new SettingUser(1));
        Role role = serv.getDbService().getRoleRepository().saveAndFlush(new Role("ex@ya.ru"));
        User user = serv.getDbService().getUserRepository().saveAndFlush(new User("nm12", "1111",
            "Петров В.В.", Map.of(
            4L, role
        ), 4L, setts));
        if(Main.test) testOn();
    }

    private void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            serv.getDbService().getUserRepository().saveAndFlush(user);
        }
    }

    private void delInv(User inv) {
        if(inv != null){
            School school = serv.getDbService().getFirstRole(inv.getRoles()).getYO();
            school.getHteachers().remove(inv);
            serv.getDbService().getSchoolRepository().saveAndFlush(school);
            serv.getDbService().getUserRepository().delete(inv);
        }
    }

    public void checkDates(){
        try {
            long now = Main.df.parse(Main.df.format(new Date())).getTime();
            for(User user : getUsers()){
                if(!ObjectUtils.isEmpty(user.getExpDate()) && now >= Main.df.parse(user.getExpDate()).getTime()){
                    System.out.println("Удалён код " + user.getCode() + " по истечению срока действия");
                    if(user.getUsername() == null) {
                        delInv(user);
                    } else {
                        delCodeUser(user);
                    }
                }
                if(user.getSettings() != null && !ObjectUtils.isEmpty(user.getSettings().getExpDateEC()) && now >= Main.df.parse(user.getSettings().getExpDateEC()).getTime()){
                    System.out.println("Удалён код email" + user.getSettings().getEmailCode() + " по истечению срока действия");
                    user.getSettings().setEmailCode(null);
                    user.getSettings().setExpDateEC(null);
                    serv.getDbService().getSettingUserRepository().saveAndFlush(user.getSettings());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public User getNUser(Role roleN, Long selRole) {
        SettingUser settingUser = serv.getDbService().getSettingUserRepository()
            .saveAndFlush(new SettingUser((int) (Math.round(Math.random() * 2) + 1)));
        setts.add(settingUser);
        Role role = serv.getDbService().getRoleRepository().saveAndFlush(roleN);
        roles.add(role);
        String fio = fakerRu.name().lastName() + " " + fakerRu.name().firstName().charAt(0) + "." + fakerRu.name().firstName().charAt(0) + ".";
        if(fakerEn.bool().bool()) {
            String uuid = UUID.randomUUID().toString();
            return serv.getDbService().getUserRepository().saveAndFlush(new User(fio, Map.of(
                selRole, role
            ), selRole, Main.df.format(dateAfter), uuid));
        } else {
            return serv.getDbService().getUserRepository()
                .saveAndFlush(new User(fakerEn.internet().slug(), fakerEn.internet().password(),
                    fio, Map.of(
                    selRole, role
                ), selRole, settingUser));
        }
    }

    private void getRandSystem() {
        Instant after = Instant.now().plus(Duration.ofDays(30));
        dateAfter = Date.from(after);
        newsList = serv.getDbService().getNewsRepository().saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец...")
        ));

        contactsList = serv.getDbService().getContactsRepository().saveAllAndFlush(asList(
            new Contacts(
                "8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg")
        ));
        syst = serv.getDbService().createSyst(new Syst(asList(newsList.get(0), newsList.get(1)), contactsList.get(0)));
        System.out.println(getSyst());

        users = new ArrayList<>();
        setts = new ArrayList<>();
        roles = new ArrayList<>();

        int max = (int) Math.round(Math.random() * 3) + 2, i;
        for(i = 0; i < max; i++) {
            User user = getNUser(new Role(fakerEn.internet().emailAddress()), 4L);
            users.add(user);
            syst.getAdmins().add(user);
        }
        syst = serv.getDbService().getSystRepository().saveAndFlush(syst);
    }

    private void getRandSchedule(School school, Group group, List<User> tea){
        int day, les;
        Lesson lesson;
        if (ObjectUtils.isEmpty(tea)) return;
        for(day = 0; day < 5; day++) {
            if(fakerEn.bool().bool()) continue;
            for(les = 0; les < 6; les++) {
                if(fakerEn.bool().bool()) continue;
                String kab = ((int) Math.round(Math.random() * 2000) + 1) + "",
                    nameSubj = namesSubj[(int) Math.round(Math.random() * 4)];
                User teaU = tea.get((int) Math.round(Math.random() * (tea.size() - 1)));
                if(!ObjectUtils.isEmpty(school.getTeachers())
                        && school.getTeachers().contains(teaU)){
                    school.getTeachers().remove(teaU);
                }
                if(!teaU.getRoles().get(2L).getSubjects().contains(nameSubj)) {
                    teaU.getRoles().get(2L).getSubjects().add(nameSubj);
                    serv.getDbService().getRoleRepository().saveAndFlush(teaU.getRoles().get(2L));
                }
                lesson = serv.getDbService().getLessonRepository()
                    .saveAndFlush(new Lesson(school, group, day, les, kab, nameSubj, teaU));
                lessons.add(lesson);
            }
        }
    }

    private List<Group> getRandGroups(School school, List<User> tea) {
        List<Group> groupsPerSch = new ArrayList<>();
        int max = (int) Math.round(Math.random() * 3) + 2, i, max1, i1,
            grI = 1,
            namI = 0,
            maxGrI = (int) Math.round(Math.random() * 3) + 1;
        for(i = 0; i < max; i++) {
            String nameGrp = grI + namesGrp[namI];
            if(maxGrI == 0) {
                grI++;
                namI = 0;
                maxGrI = (int) Math.round(Math.random() * 4) + 1;
            } else {
                namI++;
                maxGrI--;
            }
            Group group = serv.getDbService().getGroupRepository().saveAndFlush(new Group(nameGrp));

            max1 = (int) Math.round(Math.random() * 3) + 2;
            for(i1 = 0; i1 < max1; i1++) {
                User userP = getNUser(new Role(fakerEn.internet().emailAddress(), school), 1L);

                User userK1 = getNUser(new Role(fakerEn.internet().emailAddress(), school, group), 0L);
                userK1.getRole(0L).getParents().add(userP);

                User userK2 = getNUser(new Role(fakerEn.internet().emailAddress(), school, group), 0L);
                userK2.getRole(0L).getParents().add(userP);

                group.getKids().add(userK1);
                group.getKids().add(userK2);
                userP.getRole(1L).getKids().add(userK1);
                userP.getRole(1L).getKids().add(userK2);
                userP.setSelKid(userK1.getId());
                users.add(serv.getDbService().getUserRepository().saveAndFlush(userP));
                users.add(serv.getDbService().getUserRepository().saveAndFlush(userK1));
                users.add(serv.getDbService().getUserRepository().saveAndFlush(userK2));
            }

            group = serv.getDbService().getGroupRepository().saveAndFlush(group);
            groupsPerSch.add(group);

            getRandSchedule(school, group, tea);
        }
        return groupsPerSch;
    }

    private void getRandSchools() {
        groups = new ArrayList<>();
        schools = new ArrayList<>();
        periods = new ArrayList<>();
        lessons = new ArrayList<>();

        int max = (int) Math.round(Math.random() * 3) + 2, i, max1, i1;
        School school = null;
        for(i = 0; i < max; i++) {
            String nameSch = namesSch[(int) Math.round(Math.random() * 2)] + (Math.round(Math.random() * 5000) + 1);
            News news = serv.getDbService().getNewsRepository()
                .saveAndFlush(new News("Мы(" + nameSch + ") перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
            newsList.add(news);

            Contacts contacts = serv.getDbService().getContactsRepository()
                .saveAndFlush(new Contacts(
                    "8 (800) 555 35 36\n5 (353) 555 00 88",
                    nameSch + "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                    "/static/media/map.jpg"));
            contactsList.add(contacts);

            List<Period> periodsPerSch = serv.getDbService().getPeriodRepository()
                .saveAllAndFlush(asList(
                    new Period("I четверть", "01.09.22", "03.11.22"),//86L
                    new Period("II четверть", "12.11.22", "29.12.22"),//87L
                    new Period("III четверть", "12.01.22", "29.03.22"),//87L
                    new Period("IV четверть", "01.04.23", "30.08.23")//88L
                ));
            periods.addAll(periodsPerSch);

            school = serv.getDbService().getSchoolRepository()
                .saveAndFlush(new School(nameSch, asList(news), contacts, periodsPerSch));

            User user = null, userL;
            max1 = (int) Math.round(Math.random() * 3) + 2;
            for(i1 = 0; i1 < max1; i1++) {
                userL = getNUser(new Role(fakerEn.internet().emailAddress(), school), 3L);
                users.add(userL);
                school.getHteachers().add(userL);
                if(userL.getUsername() != null) user = userL;
            }
            if (user != null) {
                System.out.println("htea " + user.getUsername() + " " + user.getPassword());
                user = null;
            }

            max1 = (int) Math.round(Math.random() * 3) + 2;
            for(i1 = 0; i1 < max1; i1++) {
                userL = getNUser(new Role(fakerEn.internet().emailAddress(), Set.of(namesSubj[(int) Math.round(Math.random() * 4)]), school), 2L);
                users.add(userL);
                school.getTeachers().add(userL);
                if(userL.getUsername() != null) user = userL;
            }
            if (user != null) {
                System.out.println("tea " + user.getUsername() + " " + user.getPassword());
            }

            school.setGroups(getRandGroups(school, new ArrayList<>(school.getTeachers())));

            schools.add(serv.getDbService().getSchoolRepository().saveAndFlush(school));
        }
        System.out.println("testAcP " + serv.getActualPeriodBySchool(school));
        System.out.println("testP " + serv.getDbService().getSchoolRepository().uniqPeriodsById(school.getId()));
    }

    public void testOn() {
        getRandSystem();
        getRandSchools();
    }

    public void testOff(){
        if(!ObjectUtils.isEmpty(days)) {
            serv.getDbService().getDayRepository().deleteAll(days);
            days.clear();
        }
        if(!ObjectUtils.isEmpty(marks)) {
            serv.getDbService().getMarkRepository().deleteAll(marks);
            marks.clear();
        }
        if(syst != null) {
            serv.getDbService().getSystRepository().delete(syst);
            syst = null;
        }
        if(!ObjectUtils.isEmpty(lessons)) {
            serv.getDbService().getLessonRepository().deleteAll(lessons);
            lessons.clear();
        }
        if(!ObjectUtils.isEmpty(users)) {
            for(User user : users) {
                user.rem();
            }
            users = serv.getDbService().getUserRepository().saveAllAndFlush(users);
        }
        if(!ObjectUtils.isEmpty(roles)) {
            serv.getDbService().getRoleRepository().deleteAll(roles);
            roles.clear();
        }
        if(!ObjectUtils.isEmpty(groups)) {
            serv.getDbService().getGroupRepository().deleteAll(groups);
            groups.clear();
        }
        if(!ObjectUtils.isEmpty(schools)) {
            serv.getDbService().getSchoolRepository().deleteAll(schools);
            schools.clear();
        }
        if(!ObjectUtils.isEmpty(users)) {
            serv.getDbService().getUserRepository().deleteAll(users);
            users.clear();
        }
        if(request != null) {
            serv.getDbService().getRequestRepository().delete(request);
            request = null;
        }
        if(!ObjectUtils.isEmpty(periods)) {
            serv.getDbService().getPeriodRepository().deleteAll(periods);
            periods.clear();
        }
        if(!ObjectUtils.isEmpty(contactsList)) {
            serv.getDbService().getContactsRepository().deleteAll(contactsList);
            contactsList.clear();
        }
        if(!ObjectUtils.isEmpty(newsList)) {
            serv.getDbService().getNewsRepository().deleteAll(newsList);
            newsList.clear();
        }
        if(!ObjectUtils.isEmpty(setts)) {
            serv.getDbService().getSettingUserRepository().deleteAll(setts);
            setts.clear();
        }
    }
}
