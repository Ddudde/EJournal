package ru.configs;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Configuration
@NoArgsConstructor
public class AppConfig {
    public static final String TEST_BEARER_TOKEN = "9693b2a1-77bb-4426-8045-9f9b4395d454";
    public static final String TEST_LOGIN = "nm12";
    public static final String PATH_FIREBASE = "/e-journalfcm-firebase-auth.json";

    /** RU: Формат даты, к которой легко обратиться */
    public final static DateFormat dataFormat = new SimpleDateFormat("dd.MM.yy");

    /** RU: Формат даты, к которой легко обратиться */
    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yy");

    /** RU: Вкл/выкл режима подробного описания ошибок */
    public static boolean DEBUG = true;

    /** RU: Вкл/выкл режима генерации тестовых данных */
    public static boolean TEST = true;

    @Value("${app.debug}")
    public void setDebug(boolean debug) {
        AppConfig.DEBUG = debug;
    }

    @Value("${app.test}")
    public void setTest(boolean test) {
        AppConfig.TEST = test;
    }
}