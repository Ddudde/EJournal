package utils;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import ru.data.models.Contacts;
import ru.data.models.News;
import ru.data.models.auth.Role;
import ru.data.models.auth.User;
import ru.data.models.school.*;
import ru.security.user.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

/** RU: Класс для получения случайных сущностей и их хранения */
@NoArgsConstructor
public class RandomUtils {

    public static final String defaultDescription = "Также возможны 401, 403 и 500 коды ответов";

    public static final String[] namesSubj = {"Англ. Яз.", "Математика", "Русский Яз.", "Химия", "Физика"};

    public static final String[] namesGroup = {"1А", "1Б", "1В"};

    private final Faker fakerRu = new Faker(new Locale("ru"));

    private final Faker fakerEn = new Faker();

    private static final Gson gson = new Gson();

    public static final User parentTest = getUser(3872, "Якушева", "Аркадий", "Оксана", "esse_et", "k02o9ezp8tacrfp", false);

    public static final List<User> usersTest = asList(
        getUser(3872, "Якушева", "Аркадий", "Оксана", "esse_et", "k02o9ezp8tacrfp", false),
        getUser(1705, "Дроздов", "Андрей", "Антон", "debitis_accusantium", "9an3e5ykj8", false),
        getUser(1840, "Пестов", "Людмила", "Арина", "sed_commodi", "zjitnc71x", false),
        getUser(3225, "Никифорова", "Надежда", "Александр", "numquam_nobis", "hx45205la", false),
        getUser(9764, "Силин", "Александр", "Кира", "facere_a", "2qiasp5nsk4mq7", true),
        getUser(9764, "Силин", "Александр", "Кира", "facere_a", "2qiasp5nsk4mq7", true)
    );

    public final List<News> newsTest = new ArrayList<>(asList(
        getNews(1213L, "День рождения портала!","25.04.2022", null, "Начались первые работы"),
        getNews(352L, "А проект вышел большим...","02.12.2022", "/static/media/tuman.jpg", "Да-да, всё ещё не конец...")
    ));

    public final List<Contacts> contactsTest = new ArrayList<>(asList(
        getContacts(1213L, "8 (800) 555 35 37\n5 (353) 555 00 88",
            "Ближайшие станции метро:\nАлександровский сад, 610 м (Филёвская линия, выход 5)\nБиблиотека им. Ленина, 680 м (Сокольническая линия, выход 3)\nАрбатская, 750 м (Арбатско-Покровская линия, выход 8)",
            "/static/media/map.jpg")
    ));

    public final List<Request> requestTest = new ArrayList<>(asList(
        getRequest(352L, "mail1@mail.com", "11.11.2011", "Дроздов А.А."),
        getRequest(3872L, "mail10@mail.com", "11.01.2011", "Силин А.К."),
        getRequest(9764L, "mail11@mail.com", "01.11.2011", "Пестов Л.А.")
    ));

    public final List<Period> periods = new ArrayList<>(asList(
        new Period("I четверть", "01.09.23", "03.11.23"),
        new Period("II четверть", "12.11.23", "29.12.23"),
        new Period("III четверть", "12.01.24", "29.03.24"),
        new Period("IV четверть", "01.04.24", "30.08.24")
    ));

    private Request getRequest(long id, String email, String date, String fio) {
        Request request = new Request(email, date, fio);
        request.setId(id);
        return request;
    }

    public static Contacts getCloneContacts(Contacts contacts) {
        return (Contacts) getClone(contacts, Contacts.class);
    }

    public static User getCloneUsers(User user) {
        return (User) getClone(user, User.class);
    }

    public static Object getClone(Object object, Class className) {
        return gson.fromJson(gson.toJson(object),className);
    }

    private Contacts getContacts(long id, String contact, String text, String imgUrl) {
        Contacts contacts = new Contacts(contact, text, imgUrl);
        contacts.setId(id);
        return contacts;
    }

    private News getNews(long id, String title, String date, String img_url, String text) {
        News news = new News(title, date, img_url, text);
        news.setId(id);
        return news;
    }

    private List<Lesson> getRandLessons() {
        final List<Lesson> lessons = new ArrayList<>();
        final List<User> tea = new ArrayList<>(usersTest);
        int day, les;
        for(day = 0; day < 5; day++) {
            if(fakerEn.bool().bool()) continue;
            for(les = 0; les < 6; les++) {
                if(fakerEn.bool().bool()) continue;
                String kab = ((int) Math.round(Math.random() * 2000) + 1) + "",
                    nameSubj = namesSubj[(int) Math.round(Math.random() * 4)],
                    nameGroup = namesGroup[(int) Math.round(Math.random() * 2)];
                User teaU = tea.get((int) Math.round(Math.random() * (tea.size() - 1)));
                lessons.add(new Lesson(null, new Group(nameGroup), day, les, kab, nameSubj, teaU));
            }
        }
        return lessons;
    }

    public Group getGroup(long id, String nameGrp) {
        Group group = new Group(nameGrp);
        group.getKids().addAll(usersTest);
        group.setId(id);
//        group.setId(Math.round(Math.random() * 10000));
        return group;
    }

    public School getSchool(long id, String nameSch, long... ids) {
//        final String[] namesSch = {"Гимназия №", "Школа №", "Лицей №"};
//        String nameSch = namesSch[(int) Math.round(Math.random() * 2)] + (Math.round(Math.random() * 5000) + 1);
        List list = List.of();
        School school = new School(nameSch, list, null, list);
        school.getHteachers().addAll(usersTest);
        school.getGroups().addAll(asList(
            getGroup(ids[0], "1A"),
            getGroup(ids[1], "1Б"),
            getGroup(ids[2], "1В")
        ));
        school.setId(id);
//        school.setId(Math.round(Math.random() * 10000));
        return school;
    }

    private static User getUser(long id, String lastName, String firstName, String middleName, String slug, String password, boolean existsParents) {
        String fio = lastName + " " + firstName.charAt(0) + "." + middleName.charAt(0) + ".";
        User user = new User(slug, password, fio, mock(Map.class), null, null);
        user.setId(id);
        if(existsParents) {
            Role role = new Role();
            role.setEmail("example@mail.com");
            role.getParents().add(parentTest);
            user.getRoles().put(Roles.KID, role);
        }
        user.setSelRole(Roles.KID);
        return user;
    }
}
