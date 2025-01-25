package ru.services.db;

import com.github.javafaker.Faker;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.Main;
import ru.controllers.TestController;
import ru.data.DAO.Contacts;
import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.*;
import ru.data.SSE.Subscriber;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.MainService;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.util.Arrays.asList;
import static ru.Main.datas;

/** RU: Класс для рандомизация данных, тестовые данные для БД */
@Slf4j
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
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    /** RU: дата: +30 дней от актуальной */
    private Date dateAfter;

    private void postConstruct() {
        final SettingUser setts = datas.getDbService().getSettingUserRepository()
            .saveAndFlush(new SettingUser(1));
        final Role role = datas.getDbService().getRoleRepository()
            .saveAndFlush(new Role("ex@ya.ru"));
        final User user = datas.getDbService().getUserRepository()
            .saveAndFlush(new User("nm12", passwordEncoder.encode("1111"),
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
                    log.debug("Удалён код " + user.getCode() + " по истечению срока действия");
                    if(user.getUsername() == null) {
                        delInv(user);
                    } else {
                        delCodeUser(user);
                    }
                }
                SettingUser settingUser = user.getSettings();
                if(settingUser != null && !ObjectUtils.isEmpty(settingUser.getExpDateEC()) && now >= Main.df.parse(settingUser.getExpDateEC()).getTime()){
                    log.debug("Удалён код email" + settingUser.getEmailCode() + " по истечению срока действия");
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
    private User getNUser(Role roleN, Roles selRole, String testPassword) {
        final SettingUser settingUser = datas.getDbService().getSettingUserRepository()
            .saveAndFlush(new SettingUser(random.nextInt(2) + 1));
        setts.add(settingUser);
        final Role role = datas.getDbService().getRoleRepository().saveAndFlush(roleN);
        roles.add(role);
        final String fio = fakerRu.name().lastName() + " " + fakerRu.name().firstName().charAt(0) + "." + fakerRu.name().firstName().charAt(0) + ".";
        if(random.nextBoolean()) {
            final String uuid = UUID.randomUUID().toString();
            final User user = datas.getDbService().getUserRepository()
                .saveAndFlush(new User(fio, Map.of(
                    selRole, role
                ), selRole, Main.df.format(dateAfter), uuid));
            users.add(user);
            return user;
        } else {
            final String login = MainService.getRandomUsername(fakerEn);
            final String password = passwordEncoder.encode(testPassword);
            final User user = datas.getDbService().getUserRepository()
                .saveAndFlush(new User(login, password,
                    fio, Map.of(
                    selRole, role
                ), selRole, settingUser));
            users.add(user);
            return user;
        }
    }

    /** RU: создаёт данные системы:
     * Новости, контакты, админы-пользователи и пароль для тестовых аккаунтов */
    private void getRandSystem(String testPassword) {
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        dateAfter = Date.from(after);
        newsList = datas.getDbService().getNewsRepository().saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец...")
        ));

        contactsList = datas.getDbService().getContactsRepository()
            .saveAllAndFlush(asList(new Contacts("8 (800) 555 35 37\n5 (353) 555 00 88",
                "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg")
        ));
        final Syst systM = new Syst(newsList, contactsList.get(0));
        systM.setTestPassword(testPassword);
        syst = datas.getDbService().createSyst(systM);
        log.trace(getSyst() + "");

        users.clear();
        setts.clear();
        roles.clear();

        int max = random.nextInt(3) + 2, i;
        for(i = 0; i < max; i++) {
            final Role role = new Role(fakerEn.internet().emailAddress());
            final User user = getNUser(role, Roles.ADMIN, testPassword);
            syst.getAdmins().add(user);
        }
        syst = datas.getDbService().getSystRepository().saveAndFlush(syst);
    }

    /** RU: рандомит расписание, запускает рандом оценок и домашних заданий:
     * 6 возможных дней, 7 возможных уроков, и записывает учителю урок */
    private List<String> getRandSchedule(School school, Group group, List<User> tea){
        int day, les;
        Lesson lesson;
        final List<String> nameSubjects = new ArrayList<>();
        if (ObjectUtils.isEmpty(tea)) return null;

        for(day = 0; day < 5; day++) {
            if(random.nextBoolean()) continue;

            for(les = 0; les < 6; les++) {
                if(random.nextBoolean()) continue;

                final String kab = (random.nextInt(2000) + 1) + "";
                final String nameSubj = namesSubj[random.nextInt(5)];
                final int indexOfTeacher = random.nextInt(tea.size());
                final User teaU = tea.get(indexOfTeacher);
                if(!ObjectUtils.isEmpty(school.getTeachers())
                        && school.getTeachers().contains(teaU)){
                    school.getTeachers().remove(teaU);
                }
                if(!teaU.getRole(Roles.TEACHER).getSubjects().contains(nameSubj)) {
                    teaU.getRole(Roles.TEACHER).getSubjects().add(nameSubj);
                    datas.getDbService().getRoleRepository()
                        .saveAndFlush(teaU.getRole(Roles.TEACHER));
                }
                lesson = datas.getDbService().getLessonRepository()
                    .saveAndFlush(new Lesson(school, group, day, les, kab, nameSubj, teaU));
                lessons.add(lesson);

                nameSubjects.add(nameSubj);

                getRandomMark(group, school, nameSubj, day, teaU);
            }
        }
        return nameSubjects;
    }

    /** RU: выставляет с 10% шансом случайные оценки группе.
     * <pre>
     * В первые 4 недели актуального периода, на каждый урок в течении дня ставит оценку с 10% шанса.
     * Возможные оценки: от 1 до 5 или "Н", "Н" - оценка "не был на уроке"
     * Возможный вес оценки: от 1 до 5, "3" - самостоятельная работа, "5" - контрольная работа
     * </pre> */
    private void getRandomMark(Group group, School school, String nameSubj, int dayOfWeek, User teaU) {
        final Period period = datas.getActualPeriodBySchool(school);
        final LocalDate startDate = LocalDate.parse(period.getDateN(), Main.dateFormat);
        dayOfWeek++;
        final TemporalAdjuster adjuster = TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek));
        LocalDate nextOrSameDayOfWeek = startDate.with(adjuster);
        int plusWeek;
        for(plusWeek = 1; plusWeek < 5; plusWeek++) {
            final String dayInFormat = nextOrSameDayOfWeek.format(Main.dateFormat);
            nextOrSameDayOfWeek = nextOrSameDayOfWeek.plusWeeks(plusWeek);
            final Day day = new Day();
            day.setDat(dayInFormat);
            day.setGrp(group);
            day.setTeacher(teaU);
            day.setSchool(school);
            day.setNameSubject(nameSubj);
            if(getRandomInPercent(75)) {
                day.setHomework(getRandomHomework());
            }
            for(User kid : group.getKids()) {
                if(!getRandomInPercent(10)) continue;

                int mark = random.nextInt(1, 7);
                int weight = random.nextInt(1, 6);
                String markStr = mark + "";
                if(mark == 6) markStr = "Н";
                String style = "Ответ на уроке";
                if(weight == 3) style = "Самостоятельная работа";
                if(weight == 5) style = "Контрольная работа";
                final Mark markDAO = new Mark(kid, period, markStr, weight, "norm", style);
                datas.getDbService().getMarkRepository().saveAndFlush(markDAO);
                day.getMarks().add(markDAO);
            }
            datas.getDbService().getDayRepository().saveAndFlush(day);
        }
    }

    /** RU: c 75% шансом даёт группе домашку на определённый урок
     * @return Примеры: "Стр. 62-63 пересказ", "Упр. 6 Стр. 103" */
    private String getRandomHomework() {
        final int a = random.nextInt(1, 1000);
        final int b = random.nextInt(1, 1000);
        if(random.nextBoolean()) {
            return String.format("Упр. %s Стр. %s", a, b);
        }
        return String.format("Стр. %s-%s пересказ", a, b);
    }

    private boolean getRandomInPercent(int percent) {
        return random.nextInt(101) <= percent;
    }

    /** RU: создаёт данные групп и относящиеся к ним данные:
     * учеников, родителей, группа, расписание, оценки, итоговые оценки, домашние задания */
    private List<Group> getRandGroups(School school, List<User> tea, String testPassword) {
        final List<Group> groupsPerSch = new ArrayList<>();
        final int countOfGroups = random.nextInt(3) + 2;
        int i, i1,
            grI = 1,
            namI = 0,
            maxGrI = random.nextInt(3) + 1;
        for(i = 0; i < countOfGroups; i++) {
            final String nameGrp = grI + namesGrp[namI];
            if(maxGrI == 0) {
                grI++;
                namI = 0;
                maxGrI = random.nextInt(4) + 1;
            } else {
                namI++;
                maxGrI--;
            }
            Group group = datas.getDbService().getGroupRepository()
                .saveAndFlush(new Group(nameGrp));

            final int countOfIterationsCreatePeople = random.nextInt(3) + 2;
            for(i1 = 0; i1 < countOfIterationsCreatePeople; i1++) {
                Role roleP = new Role(fakerEn.internet().emailAddress(), school);
                final User userP = getNUser(roleP, Roles.PARENT, testPassword);

                final Role roleK1 = new Role(fakerEn.internet().emailAddress(), school, group);
                roleK1.getParents().add(userP);
                final User userK1 = getNUser(roleK1, Roles.KID, testPassword);

                final Role roleK2 = new Role(fakerEn.internet().emailAddress(), school, group);
                roleK2.getParents().add(userP);
                final User userK2 = getNUser(roleK2, Roles.KID, testPassword);

                group.getKids().add(userK1);
                group.getKids().add(userK2);
                roleP = userP.getRole(Roles.PARENT);
                roleP.getKids().add(userK1);
                roleP.getKids().add(userK2);
                datas.getDbService().getRoleRepository().saveAndFlush(roleP);
                userP.setSelKid(userK1.getId());
                datas.getDbService().getUserRepository().saveAndFlush(userP);
            }

            group = datas.getDbService().getGroupRepository().saveAndFlush(group);
            groupsPerSch.add(group);

            final List<String> nameSubjects = getRandSchedule(school, group, tea);
            getRandomMarkPers(school, group, nameSubjects);
        }
        return groupsPerSch;
    }

    /** RU: выставляет с 25% шансом случайные периодовые оценки группе.
     * <pre>
     * Во все возможные периоды учебного центра.
     * По 5 балльной шкале, от 1 до 5
     * </pre> */
    private void getRandomMarkPers(School school, Group group, List<String> nameSubjects) {
        for(Period period : school.getPeriods()) {
            for(String nameSubject : nameSubjects) {
                for(User kid : group.getKids()) {
                    if (!getRandomInPercent(25)) continue;

                    int mark = random.nextInt(1, 6);
                    String markStr = mark + "";
                    Mark markDAO = datas.getDbService().getMarkRepository()
                        .findByTypeAndStyleAndPeriodIdAndUsrId("per", nameSubject, period.getId(), kid.getId());
                    if(markDAO == null) {
                        markDAO = new Mark();
                        markDAO.setUsr(kid);
                        markDAO.setPeriod(period);
                        markDAO.setType("per");
                        markDAO.setWeight(1);
                        markDAO.setStyle(nameSubject);
                    }
                    markDAO.setMark(markStr);
                    datas.getDbService().getMarkRepository().saveAndFlush(markDAO);
                }
            }
        }
    }

    /** RU: создаёт данные школ и относящиеся к ним данные:
     * школа, новости, контакты, периоды обучения, групп
     * пользователей: завучей, учителей, учеников, родителей*/
    private void getRandSchools(String testPassword) {
        groups.clear();
        schools.clear();
        periods.clear();
        lessons.clear();

        final int countOfSchool = random.nextInt(3) + 2;
        int i, i1;
        School school = null;
        for(i = 0; i < countOfSchool; i++) {
            final String nameSch = namesSch[random.nextInt(3)] + (random.nextInt(5000) + 1);
            final News news = datas.getDbService().getNewsRepository()
                .saveAndFlush(new News("Мы(" + nameSch + ") перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
            newsList.add(news);

            final Contacts contacts = datas.getDbService().getContactsRepository()
                .saveAndFlush(new Contacts(
                    "8 (800) 555 35 36\n5 (353) 555 00 88",
                    nameSch + "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                    "/static/media/map.jpg"));
            contactsList.add(contacts);

            final List<Period> periodsPerSch = datas.getDbService().getPeriodRepository()
                .saveAllAndFlush(asList(
                    new Period("I четверть", "01.09.24", "03.11.24"),
                    new Period("II четверть", "12.11.24", "29.12.24"),
                    new Period("III четверть", "12.01.25", "29.03.25"),
                    new Period("IV четверть", "01.04.25", "30.08.25")
                ));
            periods.addAll(periodsPerSch);

            school = datas.getDbService().getSchoolRepository()
                .saveAndFlush(new School(nameSch, asList(news), contacts, periodsPerSch));

            final int countIterationsCreateHteachers = random.nextInt(3) + 2;
            User user = null, userL;
            for(i1 = 0; i1 < countIterationsCreateHteachers; i1++) {
                final Role role = new Role(fakerEn.internet().emailAddress(), school);
                userL = getNUser(role, Roles.HTEACHER, testPassword);
                school.getHteachers().add(userL);
                if(userL.getUsername() != null) user = userL;
            }
            if (user != null) {
                log.trace("htea " + user.getUsername() + " " + user.getPassword());
                user = null;
            }

            final int countIterationCreateTeachers = random.nextInt(3) + 2;
            for(i1 = 0; i1 < countIterationCreateTeachers; i1++) {
                final Role role = new Role(fakerEn.internet().emailAddress(), Set.of(namesSubj[(int) Math.round(Math.random() * 4)]), school);
                userL = getNUser(role, Roles.TEACHER, testPassword);
                school.getTeachers().add(userL);
                if(userL.getUsername() != null) user = userL;
            }
            if (user != null) {
                log.trace("tea " + user.getUsername() + " " + user.getPassword());
            }

            school.setGroups(getRandGroups(school, new ArrayList<>(school.getTeachers()), testPassword));

            schools.add(datas.getDbService().getSchoolRepository().saveAndFlush(school));
        }
    }

    /** RU: рандомизируются данные системы(новости, админы), создаются несколько школ
     * и сохраняются в базу данных */
    public void testOn() {
        final String testPassword = fakerEn.internet().password();
        getRandSystem(testPassword);
        getRandSchools(testPassword);
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
     *     "code"
     * }
     * </pre>
     * @exception Exception Исключение вызывается при ошибках с Json
     * @see #getTestInfo(JsonTreeWriter) Пример использования */
    private void getUsersT(JsonTreeWriter wrtr, List<User> users) throws Exception {
        for (User user : users) {
            wrtr.name(user.getId()+"").beginObject()
                .name("fio").value(user.getFio())
                .name("login").value(user.getUsername())
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
     * @see TestController#getInfo(Subscriber, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
    public void getTestInfo(JsonTreeWriter wrtr) throws Exception {
        wrtr.name("bodyT").beginObject();
        if(getSyst() != null) {
            wrtr.name("testPassword").value(getSyst().getTestPassword());
        }
        wrtr.name("admins").beginObject();
        if(getSyst() != null) {
            getUsersT(wrtr, getSyst().getAdmins());
        }
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
