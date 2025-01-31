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
import ru.data.DTO.SubscriberDTO;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
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
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/** RU: Класс для рандомизация данных, тестовые данные для БД */
@Slf4j
@Getter @Setter
@RequiredArgsConstructor
@Service public class IniDBService {
    private final PasswordEncoder passwordEncoder;
    private final SettingUserRepository settingUserRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final DBService dbService;
    private final NewsRepository newsRepository;
    private final ContactsRepository contactsRepository;
    private final SystRepository systRepository;
    private final DayRepository dayRepository;
    private final LessonRepository lessonRepository;
    private final MarkRepository markRepository;
    private final GroupRepository groupRepository;
    private final PeriodRepository periodRepository;
    private final RequestRepository requestRepository;
    private final MainService mainService;
    private final Set<Long> setts = new HashSet<>();
    private Set<Long> newsSet = new HashSet<>();
    private final Set<Long> contactsSet = new HashSet<>();
    private final Set<Long> periods = new HashSet<>();
    private final Set<Long> groups = new HashSet<>();
    private final Set<Long> schools = new HashSet<>();
    private final Set<Long> lessons = new HashSet<>();
    private final Set<Long> roles = new HashSet<>();
    private Set<Long> users = new HashSet<>();
    private Request request;
    private Syst syst;
    private final Set<Long> marks = new HashSet<>();
    private final Set<Long> days = new HashSet<>();
    private final Faker fakerRu = new Faker(new Locale("ru"));
    private final Faker fakerEn = new Faker();
    private final String[] namesSch = {"Гимназия №", "Школа №", "Лицей №"};
    private final String[] namesGrp = {"А", "Б", "В", "Г", "Д"};
    private final String[] namesSubj = {"Англ. Яз.", "Математика", "Русский Яз.", "Химия", "Физика"};
    private final Random random = new Random();

    /** RU: дата: +30 дней от актуальной */
    private Date dateAfter;

    private void postConstruct() {
        final SettingUser setts = settingUserRepository.saveAndFlush(new SettingUser(1));
        final Role role = roleRepository.saveAndFlush(new Role("ex@ya.ru"));
        final User user = userRepository
            .saveAndFlush(new User("nm12", passwordEncoder.encode("1111"),
            "Петров В.В.", Map.of(
            Roles.ADMIN, role
        ), Roles.ADMIN, setts));
        if(MainService.test) testOn();
        checkDates();
    }

    /** RU: удаление инвайта для новой роли */
    private void delCodeUser(User user){
        if(user != null){
            user.setCode(null);
            user.setExpDate(null);
            userRepository.saveAndFlush(user);
        }
    }

    /** RU: удаление инвайта для регистрации нового пользователя */
    private void delInv(User inv) {
        if(inv != null) {
            final School school = dbService.getFirstRole(inv.getRoles()).getYO();
            school.getHteachers().remove(inv);
            schoolRepository.saveAndFlush(school);
            userRepository.delete(inv);
        }
    }

    /** RU: проверяет коды подтверждений электронных почт и инвайтов на истечение срока */
    private void checkDates() {
        try {
            final long now = MainService.df.parse(MainService.df.format(new Date())).getTime();
            final List<User> listUsers = userRepository.findAllById(users);
            for(User user : listUsers) {
                if(!ObjectUtils.isEmpty(user.getExpDate()) && now >= MainService.df.parse(user.getExpDate()).getTime()){
                    log.debug("Удалён код " + user.getCode() + " по истечению срока действия");
                    if(user.getUsername() == null) {
                        delInv(user);
                    } else {
                        delCodeUser(user);
                    }
                }
                final SettingUser settingUser = user.getSettings();
                if(settingUser != null && !ObjectUtils.isEmpty(settingUser.getExpDateEC()) && now >= MainService.df.parse(settingUser.getExpDateEC()).getTime()){
                    log.debug("Удалён код email" + settingUser.getEmailCode() + " по истечению срока действия");
                    settingUser.setEmailCode(null);
                    settingUser.setExpDateEC(null);
                    settingUserRepository.saveAndFlush(settingUser);
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
        final SettingUser settingUser = settingUserRepository
            .saveAndFlush(new SettingUser(random.nextInt(2) + 1));
        setts.add(settingUser.getId());
        final Role role = roleRepository.saveAndFlush(roleN);
        roles.add(role.getId());
        final String fio = fakerRu.name().lastName() + " " + fakerRu.name().firstName().charAt(0) + "." + fakerRu.name().firstName().charAt(0) + ".";
        if(random.nextBoolean()) {
            final String uuid = UUID.randomUUID().toString();
            final User user = userRepository
                .saveAndFlush(new User(fio, Map.of(
                    selRole, role
                ), selRole, MainService.df.format(dateAfter), uuid));
            users.add(user.getId());
            return user;
        } else {
            final String login = MainService.getRandomUsername(fakerEn);
            final String password = passwordEncoder.encode(testPassword);
            final User user = userRepository
                .saveAndFlush(new User(login, password,
                    fio, Map.of(
                    selRole, role
                ), selRole, settingUser));
            users.add(user.getId());
            return user;
        }
    }

    /** RU: создаёт данные системы:
     * Новости, контакты, админы-пользователи и пароль для тестовых аккаунтов */
    private void getRandSystem(String testPassword) {
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        dateAfter = Date.from(after);
        newsSet = newsRepository.saveAllAndFlush(asList(
            new News("День рождения портала!","25.04.2022", "Начались первые работы"),
            new News("А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец...")
        )).stream()
            .map(News::getId).collect(Collectors.toCollection(HashSet::new));

        final Contacts contacts = new Contacts("8 (800) 555 35 37\n5 (353) 555 00 88",
            "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
            "/static/media/map.jpg");
        contactsSet.add(contactsRepository.saveAndFlush(contacts).getId());
        final Syst systM = new Syst(newsRepository.findAllById(newsSet), contacts);
        systM.setTestPassword(testPassword);
        syst = dbService.createSyst(systM);
        log.trace(syst + "");

        users.clear();
        setts.clear();
        roles.clear();

        int max = random.nextInt(3) + 2, i;
        for(i = 0; i < max; i++) {
            final Role role = new Role(fakerEn.internet().emailAddress());
            final User user = getNUser(role, Roles.ADMIN, testPassword);
            syst.getAdmins().add(user);
        }
        syst = systRepository.saveAndFlush(syst);
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
                    roleRepository.saveAndFlush(teaU.getRole(Roles.TEACHER));
                }
                lesson = lessonRepository
                    .saveAndFlush(new Lesson(school, group, day, les, kab, nameSubj, teaU));
                lessons.add(lesson.getId());

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
        final Period period = mainService.getActualPeriodBySchool(school);
        final LocalDate startDate = LocalDate.parse(period.getDateN(), MainService.dateFormat);
        dayOfWeek++;
        final TemporalAdjuster adjuster = TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek));
        LocalDate nextOrSameDayOfWeek = startDate.with(adjuster);
        int plusWeek;
        for(plusWeek = 1; plusWeek < 5; plusWeek++) {
            final String dayInFormat = nextOrSameDayOfWeek.format(MainService.dateFormat);
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

                final int mark = random.nextInt(1, 7);
                final int weight = random.nextInt(1, 6);
                String markStr = mark + "";
                if(mark == 6) markStr = "Н";
                String style = "Ответ на уроке";
                if(weight == 3) style = "Самостоятельная работа";
                if(weight == 5) style = "Контрольная работа";
                final Mark markDAO = new Mark(kid, period, markStr, weight, "norm", style);
                markRepository.saveAndFlush(markDAO);
                day.getMarks().add(markDAO);
            }
            days.add(dayRepository.saveAndFlush(day).getId());
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
            Group group = groupRepository.saveAndFlush(new Group(nameGrp));

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
                roleRepository.saveAndFlush(roleP);
                userP.setSelKid(userK1.getId());
                userRepository.saveAndFlush(userP);
            }

            group = groupRepository.saveAndFlush(group);
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

                    final int mark = random.nextInt(1, 6);
                    final String markStr = mark + "";
                    boolean isExist = true;
                    Mark markDAO = markRepository
                        .findByTypeAndStyleAndPeriodIdAndUsrId("per", nameSubject, period.getId(), kid.getId());
                    if(markDAO == null) {
                        isExist = false;
                        markDAO = new Mark();
                        markDAO.setUsr(kid);
                        markDAO.setPeriod(period);
                        markDAO.setType("per");
                        markDAO.setWeight(1);
                        markDAO.setStyle(nameSubject);
                    }
                    markDAO.setMark(markStr);
                    markRepository.saveAndFlush(markDAO);
                    if(!isExist) {
                        marks.add(markDAO.getId());
                    }
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
        days.clear();
        marks.clear();

        final int countOfSchool = random.nextInt(3) + 2;
        int i, i1;
        School school = null;
        for(i = 0; i < countOfSchool; i++) {
            final String nameSch = namesSch[random.nextInt(3)] + (random.nextInt(5000) + 1);
            final News news = newsRepository
                .saveAndFlush(new News("Мы(" + nameSch + ") перешли на этот сервис","11.11.2022", "Всем своим дружным коллективом мы остановились на данном варианте."));
            newsSet.add(news.getId());

            final Contacts contacts = contactsRepository.saveAndFlush(new Contacts(
                "8 (800) 555 35 36\n5 (353) 555 00 88",
                nameSch + "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
                "/static/media/map.jpg"));
            contactsSet.add(contacts.getId());

            final List<Period> periodsPerSch = periodRepository.saveAllAndFlush(asList(
                new Period("I четверть", "01.09.24", "03.11.24"),
                new Period("II четверть", "12.11.24", "29.12.24"),
                new Period("III четверть", "12.01.25", "29.03.25"),
                new Period("IV четверть", "01.04.25", "30.08.25")
            ));
            periods.addAll(periodsPerSch.stream()
                    .map(Period::getId).collect(Collectors.toCollection(HashSet::new)));

            school = schoolRepository
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

            schools.add(schoolRepository.saveAndFlush(school).getId());
        }
    }

    /** RU: рандомизируются данные системы(новости, админы), создаются несколько школ
     * и сохраняются в базу данных */
    public void testOn() {
        final String testPassword = fakerEn.internet().password();
        getRandSystem(testPassword);
        getRandSchools(testPassword);
    }

    /** RU: очищаются/удаляются все тестовые данные */
    public void testOff(){
        if(!ObjectUtils.isEmpty(days)) {
            dayRepository.deleteAllById(days);
            days.clear();
        }
        if(!ObjectUtils.isEmpty(marks)) {
            markRepository.deleteAllById(marks);
            marks.clear();
        }
        if(syst != null) {
            systRepository.delete(syst);
            syst = null;
        }
        if(!ObjectUtils.isEmpty(lessons)) {
            lessonRepository.deleteAllById(lessons);
            lessons.clear();
        }
        if(!ObjectUtils.isEmpty(users) && !ObjectUtils.isEmpty(roles)) {
            final List<User> listUsers = userRepository.findAllById(users);
            for(User user : listUsers) {
                user.getRoles().clear();
            }
            userRepository.saveAllAndFlush(listUsers);
            roleRepository.deleteAllById(roles);
            roles.clear();
        }
//        if(!ObjectUtils.isEmpty(roles)) {
//            roleRepository.deleteAllById(roles);
//            roles.clear();
//        }
        if(!ObjectUtils.isEmpty(groups)) {
//            groupRepository.deleteAll(groups);
            groups.clear();
        }
        if(!ObjectUtils.isEmpty(schools)) {
            schoolRepository.deleteAllById(schools);
            schools.clear();
        }
        if(!ObjectUtils.isEmpty(users)) {
            userRepository.deleteAllById(users);
            users.clear();
        }
        if(request != null) {
            requestRepository.delete(request);
            request = null;
        }
        if(!ObjectUtils.isEmpty(periods)) {
//            periodRepository.deleteAll(periods);
            periods.clear();
        }
        if(!ObjectUtils.isEmpty(contactsSet)) {
//            contactsRepository.deleteAll(contactsList);
            contactsSet.clear();
        }
        if(!ObjectUtils.isEmpty(newsSet)) {
//            newsRepository.deleteAll(newsList);
            newsSet.clear();
        }
        if(!ObjectUtils.isEmpty(setts)) {
//            settingUserRepository.deleteAll(setts);
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
     * @see TestController#getInfo(SubscriberDTO, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
    public void getTestInfo(JsonTreeWriter wrtr) throws Exception {
        wrtr.name("bodyT").beginObject();
        if(syst != null) {
            wrtr.name("testPassword").value(syst.getTestPassword());
        }
        wrtr.name("admins").beginObject();
        if(syst != null) {
            getUsersT(wrtr, syst.getAdmins());
        }
        wrtr.endObject()
            .name("schools").beginObject();
        if(!ObjectUtils.isEmpty(schools)) {
            final List<School> listSchools = schoolRepository.findAllById(schools);
            for (School school : listSchools) {
                wrtr.name(school.getId()+"").beginObject()
                    .name("name").value(school.getName())
                    .name("hteachers").beginObject();
                getUsersT(wrtr, school.getHteachers());
                wrtr.endObject()
                    .name("teachers").beginObject();
                final List<User> teachersUBySchool = lessonRepository.uniqTeachersUBySchool(school.getId());
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
