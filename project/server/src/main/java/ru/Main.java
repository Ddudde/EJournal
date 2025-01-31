package ru;

import lombok.extern.slf4j.Slf4j;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.data.DAO.school.School;
import ru.services.MainService;

import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create;

/** RU: Главный класс, запуск сервера */
@Slf4j
@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
public class Main {

    /** RU: Контекст */
    public static ConfigurableApplicationContext ctx;

    /** RU: Входная точка, запускает спринг */
    public static void main(String... args) {
        ctx = SpringApplication.run(Main.class);
        log.trace("Hello world!");
    }

    /** RU: Показывает исключение и в зависимости от свойства {@link MainService#debug} с подробностью
     * @see MainService#getActualPeriodBySchool(School) Пример использования*/
    public static boolean excp(Exception e) {
        if(MainService.debug) {
            e.printStackTrace();
        } else {
            log.debug(e.getMessage());
        }
        return false;
    }

    /** RU: публикует asciiDoc в формате PDF и HTML
     * @see #main(String[]) Пример использования*/
    private static void genAsciiDoc() {
        try(Asciidoctor asciidoctor = create()) {
            final OptionsBuilder options = options().inPlace(true)
//            .backend("pdf");
                .backend("html");
            String[] results = asciidoctor.convertDirectory(new AsciiDocDirectoryWalker("docs/asciiDoc"), options);
        }
    }
}