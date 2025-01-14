package ru.services.db;

import com.github.javafaker.Faker;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.Main;
import ru.controllers.TestController;
import ru.data.models.Contacts;
import ru.data.models.News;
import ru.data.models.Syst;
import ru.data.models.auth.Role;
import ru.data.models.auth.SettingUser;
import ru.data.models.auth.User;
import ru.data.models.school.*;
import ru.security.user.Roles;
import ru.services.MainService;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

/** RU: Класс для рандомизация данных, тестовые данные для БД
 * <pre>
 * beenDo: Сделано
 *  + Переписка
 *  + Javadoc
 *  + Тестирование
 * </pre> */
@Getter @Setter
@RequiredArgsConstructor
@Service public class IniDBService {

    private final List<SettingUser> setts = new ArrayList<>();

    private List<News> newsList = new ArrayList<>();

    private List<Contacts> contactsList = new ArrayList<>();

    private final List<Period> periods = new ArrayList<>();

    private final List<Group> groups = new ArrayList<>();

    private final List<School> schools = new ArrayList<>();

    private final List<Lesson> lessons = new ArrayList<>();

    private final List<Role> roles = new ArrayList<>();

    private List<User> users = new ArrayList<>();

    private Request request;

    private Syst syst;

    private final List<Mark> marks = new ArrayList<>();

    private final List<Day> days = new ArrayList<>();

    private final Faker fakerRu = new Faker(new Locale("ru"));

    private final Faker fakerEn = new Faker();

    private final String[] namesSch = {"Гимназия №", "Школа №", "Лицей №"};

    private final String[] namesGrp = {"А", "Б", "В", "Г", "Д"};

    private final String[] namesSubj = {"Англ. Яз.", "Математика", "Русский Яз.", "Химия", "Физика"};

    /** RU: дата: +30 дней от актуальной */
    private Date dateAfter;

    private final MainService datas;

    private void postConstruct() {
        SettingUser setts = datas.getDbService().getSettingUserRepository().saveAndFlush(new SettingUser(1));
        Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role("ex@ya.ru"));
        User user = datas.getDbService().getUserRepository().saveAndFlush(new User("nm12", "1111",
            "Петров В.В.", Map.of(
            Roles.ADMIN, role
        ), Roles.ADMIN, setts));
        if(Main.test) testOn();
        checkDates();
    }

    /** RU: удаление инвайта для новой роли */
    private void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            datas.getDbService().getUserRepository().saveAndFlush(user);
        }
    }

    /** RU: удаление инвайта для регистрации нового пользователя */
    private void delInv(User inv) {
        if(inv != null) {
            School school = datas.getDbService().getFirstRole(inv.getRoles()).getYO();
            school.getHteachers().remove(inv);
            datas.getDbService().getSchoolRepository().saveAndFlush(school);
            datas.getDbService().getUserRepository().delete(inv);
        }
    }

    /** RU: проверяет коды подтверждений электронных почт и инвайтов на истечение срока */
    private void checkDates() {
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
                SettingUser settingUser = user.getSettings();
                if(settingUser != null && !ObjectUtils.isEmpty(settingUser.getExpDateEC()) && now >= Main.df.parse(settingUser.getExpDateEC()).getTime()){
                    System.out.println("Удалён код email" + settingUser.getEmailCode() + " по истечению срока действия");
                    settingUser.setEmailCode(null);
                    settingUser.setExpDateEC(null);
                    datas.getDbService().getSettingUserRepository().saveAndFlush(settingUser);
                }
            }
        } catch (ParseException e) {
            Main.excp(e);
        }
    }

    /** RU: создаёт данные пользователя:
     * настройки, роль
     * @param roleN Роль пользователя
     * @param selRole Выбранная роль пользователя */
    private User getNUser(Role roleN, Roles selRole) {
        SettingUser settingUser = datas.getDbService().getSettingUserRepository()
            .saveAndFlush(new SettingUser((int) (Math.round(Math.random() * 2) + 1)));
        setts.add(settingUser);
        Role role = datas.getDbService().getRoleRepository().saveAndFlush(roleN);
        roles.add(role);
        String fio = fakerRu.name().lastName() + " " + fakerRu.name().firstName().charAt(0) + "." + fakerRu.name().firstName().charAt(0) + ".";
        if(fakerEn.bool().bool()) {
            String uuid = UUID.randomUUID().toString();
            return datas.getDbService().getUserRepository().saveAndFlush(new User(fio, Map.of(
                selRole, role
            ), selRole, Main.df.format(dateAfter), uuid));
        } else {
            return datas.getDbService().getUserRepository()
                .saveAndFlush(new User(MainService.getRandomUsername(fakerEn), fakerEn.internet().password(),
                    fio, Map.of(
                    selRole, role
                ), selRole, settingUser));
        }
    }

    /** RU: создаёт данные системы:
     * Новости, контакты, админы-пользователи */
    private void getRandSystem() {
        Instant after = Instant.now().plus(Duration.ofDays(30));
        dateAfter = Date.from(after);
        newsList = datas.getDbService().getNewsRepository().saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец...")
        ));

        contactsList = datas.getDbService().getContactsRepository().saveAllAndFlush(asList(
            new Contacts(
                "8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg")
        ));
        syst = datas.getDbService().createSyst(new Syst(newsList, contactsList.get(0)));
        System.out.println(getSyst());

        users.clear();
        setts.clear();
        roles.clear();

        int max = (int) Math.round(Math.random() * 3) + 2, i;
        for(i = 0; i < max; i++) {
            User user = getNUser(new Role(fakerEn.internet().emailAddress()), Roles.ADMIN);
            users.add(user);
            syst.getAdmins().add(user);
        }
        syst = datas.getDbService().getSystRepository().saveAndFlush(syst);
    }

    /** RU: рандомит расписание:
     * 6 возможных дней, 7 возможных уроков, и записывает учителю урок */
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
                if(!teaU.getRoles().get(Roles.TEACHER).getSubjects().contains(nameSubj)) {
                    teaU.getRoles().get(Roles.TEACHER).getSubjects().add(nameSubj);
                    datas.getDbService().getRoleRepository().saveAndFlush(teaU.getRoles().get(Roles.TEACHER));
                }
                lesson = datas.getDbService().getLessonRepository()
                    .saveAndFlush(new Lesson(school, group, day, les, kab, nameSubj, teaU));
                lessons.add(lesson);
            }
        }
    }

    /** RU: создаёт данные групп и относящиеся к ним данные:
     * учеников, родителей, группа, расписание */
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
            Group group = datas.getDbService().getGroupRepository().saveAndFlush(new Group(nameGrp));

            max1 = (int) Math.round(Math.random() * 3) + 2;
            for(i1 = 0; i1 < max1; i1++) {
                User userP = getNUser(new Role(fakerEn.internet().emailAddress(), school), Roles.PARENT);

                User userK1 = getNUser(new Role(fakerEn.internet().emailAddress(), school, group), Roles.KID);
                userK1.getRole(Roles.KID).getParents().add(userP);

                User userK2 = getNUser(new Role(fakerEn.internet().emailAddress(), school, group), Roles.KID);
                userK2.getRole(Roles.KID).getParents().add(userP);

                group.getKids().add(userK1);
                group.getKids().add(userK2);
                userP.getRole(Roles.PARENT).getKids().add(userK1);
                userP.getRole(Roles.PARENT).getKids().add(userK2);
                userP.setSelKid(userK1.getId());
                users.add(datas.getDbService().getUserRepository().saveAndFlush(userP));
                users.add(datas.getDbService().getUserRepository().saveAndFlush(userK1));
                users.add(datas.getDbService().getUserRepository().saveAndFlush(userK2));
            }

            group = datas.getDbService().getGroupRepository().saveAndFlush(group);
            groupsPerSch.add(group);

            getRandSchedule(school, group, tea);
        }
        return groupsPerSch;
    }

    /** RU: создаёт данные школ и относящиеся к ним данные:
     * школа, новости, контакты, периоды обучения, групп
     * пользователей: завучей, учителей, учеников, родителей*/
    private void getRandSchools() {
        groups.clear();
        schools.clear();
        periods.clear();
        lessons.clear();

        int max = (int) Math.round(Math.random() * 3) + 2, i, max1, i1;
        School school = null;
        for(i = 0; i < max; i++) {
            String nameSch = namesSch[(int) Math.round(Math.random() * 2)] + (Math.round(Math.random() * 5000) + 1);
            News news = datas.getDbService().getNewsRepository()
                .saveAndFlush(new News("Мы(" + nameSch + ") перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
            newsList.add(news);

            Contacts contacts = datas.getDbService().getContactsRepository()
                .saveAndFlush(new Contacts(
                    "8 (800) 555 35 36\n5 (353) 555 00 88",
                    nameSch + "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                    "/static/media/map.jpg"));
            contactsList.add(contacts);

            List<Period> periodsPerSch = datas.getDbService().getPeriodRepository()
                .saveAllAndFlush(asList(
                    new Period("I четверть", "01.09.23", "03.11.23"),
                    new Period("II четверть", "12.11.23", "29.12.23"),
                    new Period("III четверть", "12.01.24", "29.03.24"),
                    new Period("IV четверть", "01.04.24", "30.08.24")
                ));
            periods.addAll(periodsPerSch);

            school = datas.getDbService().getSchoolRepository()
                .saveAndFlush(new School(nameSch, asList(news), contacts, periodsPerSch));

            User user = null, userL;
            max1 = (int) Math.round(Math.random() * 3) + 2;
            for(i1 = 0; i1 < max1; i1++) {
                userL = getNUser(new Role(fakerEn.internet().emailAddress(), school), Roles.HTEACHER);
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
                userL = getNUser(new Role(fakerEn.internet().emailAddress(), Set.of(namesSubj[(int) Math.round(Math.random() * 4)]), school), Roles.TEACHER);
                users.add(userL);
                school.getTeachers().add(userL);
                if(userL.getUsername() != null) user = userL;
            }
            if (user != null) {
                System.out.println("tea " + user.getUsername() + " " + user.getPassword());
            }

            school.setGroups(getRandGroups(school, new ArrayList<>(school.getTeachers())));

            schools.add(datas.getDbService().getSchoolRepository().saveAndFlush(school));
        }
    }

    /** RU: рандомизируются данные системы(новости, админы), создаются несколько школ
     * и сохраняются в базу данных */
    public void testOn() {
        getRandSystem();
        getRandSchools();
    }

    /** RU: очищаются/удаляются все данные */
    public void testOff(){
        if(!ObjectUtils.isEmpty(days)) {
            datas.getDbService().getDayRepository().deleteAll(days);
            days.clear();
        }
        if(!ObjectUtils.isEmpty(marks)) {
            datas.getDbService().getMarkRepository().deleteAll(marks);
            marks.clear();
        }
        if(syst != null) {
            datas.getDbService().getSystRepository().delete(syst);
            syst = null;
        }
        if(!ObjectUtils.isEmpty(lessons)) {
            datas.getDbService().getLessonRepository().deleteAll(lessons);
            lessons.clear();
        }
        if(!ObjectUtils.isEmpty(users)) {
            for(User user : users) {
                user.rem();
            }
            users = datas.getDbService().getUserRepository().saveAllAndFlush(users);
        }
        if(!ObjectUtils.isEmpty(roles)) {
            datas.getDbService().getRoleRepository().deleteAll(roles);
            roles.clear();
        }
        if(!ObjectUtils.isEmpty(groups)) {
            datas.getDbService().getGroupRepository().deleteAll(groups);
            groups.clear();
        }
        if(!ObjectUtils.isEmpty(schools)) {
            datas.getDbService().getSchoolRepository().deleteAll(schools);
            schools.clear();
        }
        if(!ObjectUtils.isEmpty(users)) {
            datas.getDbService().getUserRepository().deleteAll(users);
            users.clear();
        }
        if(request != null) {
            datas.getDbService().getRequestRepository().delete(request);
            request = null;
        }
        if(!ObjectUtils.isEmpty(periods)) {
            datas.getDbService().getPeriodRepository().deleteAll(periods);
            periods.clear();
        }
        if(!ObjectUtils.isEmpty(contactsList)) {
            datas.getDbService().getContactsRepository().deleteAll(contactsList);
            contactsList.clear();
        }
        if(!ObjectUtils.isEmpty(newsList)) {
            datas.getDbService().getNewsRepository().deleteAll(newsList);
            newsList.clear();
        }
        if(!ObjectUtils.isEmpty(setts)) {
            datas.getDbService().getSettingUserRepository().deleteAll(setts);
            setts.clear();
        }
    }

    /** RU: готовит JSON с данными списка пользователей
     * <pre>
     * user.id : {
     *     "fio",
     *     "login",
     *     "pass",
     *     "code",
     * }
     * </pre>
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see #getTestInfo(JsonTreeWriter) Пример использования */
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

    /** RU: готовит JSON с данными приготовленными для тестирования по всей системе
     * <pre>
     * "bodyT" : {
     *     "admins" :{{@link #getUsersT}},
     *     "schools" :{
     *         school.id : {
     *             "name",
     *             "hteachers" :{{@link #getUsersT}},
     *             "teachers" :{{@link #getUsersT}},
     *             "groups" :{
     *                 group.id : {
     *                     "name",
     *                     "kids" :{{@link #getUsersT}},
     *                     "parents" :{{@link #getUsersT}}
     *                 }
     *             }
     *         }
     *     }
     * }
     * </pre>
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see TestController#getInfo(ru.mirea.controllers.DataTest) Пример использования */
    @SuppressWarnings("JavadocReference")
    public void getTestInfo(JsonTreeWriter wrtr) throws Exception {
        wrtr.name("bodyT").beginObject()
            .name("admins").beginObject();
        if(getSyst() != null) getUsersT(wrtr, getSyst().getAdmins());
        wrtr.endObject()
            .name("schools").beginObject();
        if(getSchools() != null) {
            for (School school : getSchools()) {
                wrtr.name(school.getId()+"").beginObject()
                    .name("name").value(school.getName())
                    .name("hteachers").beginObject();
                getUsersT(wrtr, school.getHteachers());
                wrtr.endObject()
                    .name("teachers").beginObject();
                List<User> teachersUBySchool = datas.getDbService().getLessonRepository().uniqTeachersUBySchool(school.getId());
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
                        if(!user.getRoles().containsKey(Roles.KID)) continue;
                        getUsersT(wrtr, user.getRole(Roles.KID).getParents());
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
