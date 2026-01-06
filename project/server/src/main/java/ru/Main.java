package ru;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

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

    /** RU: публикует asciiDoc в формате PDF и HTML
     * @see #main(String[]) Пример использования*/
    private static void genAsciiDoc() {
//        try(Asciidoctor asciidoctor = create()) {
//            final OptionsBuilder options = options().inPlace(true)
//            .backend("pdf");
//                .backend("html");
//            String[] results = asciidoctor.convertDirectory(new AsciiDocDirectoryWalker("docs/asciiDoc"), options);
//        }
    }
}