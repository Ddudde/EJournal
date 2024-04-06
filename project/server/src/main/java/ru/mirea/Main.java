package ru.mirea;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.mirea.data.models.school.School;
import ru.mirea.services.MainService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create;

/** RU: Главный класс, запуск сервера
 * <pre>
 * beenDo: Сделано
 * + Javadoc
 * + Переписка
 * # Тестирование
 * </pre> */
@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
public class Main {

    /** RU: Контекст */
    public static ConfigurableApplicationContext ctx;

    /** RU: Вкл/выкл режима подробного описания ошибок */
    public static boolean debug = true;

    /** RU: Вкл/выкл режима генерации тестовых данных */
    public static boolean test = false;

    /** RU: Часто используемый сервис */
    public static MainService datas;

    /** RU: Формат даты, к которой легко обратиться */
    public final static DateFormat df = new SimpleDateFormat("dd.MM.yy");

    /** RU: Входная точка, запускает спринг */
    public static void main(String... args) {
        ctx = SpringApplication.run(Main.class);
        System.out.println("Hello world!");
        datas = (MainService) ctx.getBean("mainService");
//        User user = datas.getDbService().userByLogin("nm12");
//        System.out.println(user);
//        School school = datas.getDbService().schoolById(26L);
//        System.out.println(school);
//        serverService.getDbService().getDayRepository().uniqDatAndMarksByParams();
//        serverService.getEmailService().sendRegCode("myratoff25@gmail.com");
//        serverService.sendSimpleMessage("myratoff25@gmail.com", "TestG2", "test");
//        serverService.sendSimpleMessage("nat.muradov@yandex.com", "TestY2", "test");
//        genAsciiDoc();
    }

    /** RU: Показывает исключение и в зависимости от свойства {@link #debug} с подробностью
     * @see MainService#getActualPeriodBySchool(School) Пример использования*/
    public static boolean excp(Exception e) {
        if(debug) {
            e.printStackTrace();
        } else {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /** RU: публикует asciiDoc в формате PDF и HTML
     * @see #main(String[]) Пример использования*/
    private static void genAsciiDoc() {
        //test comm
        Asciidoctor asciidoctor = create();
        final OptionsBuilder options = options().inPlace(true)
//            .backend("pdf");
          .backend("html");
        String[] results = asciidoctor.convertDirectory(new AsciiDocDirectoryWalker("docs/asciiDoc"), options);
    }
}