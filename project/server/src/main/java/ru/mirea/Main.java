package ru.mirea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
public class Main {

    private static ConfigurableApplicationContext ctx;

    private final static boolean debug = true;

    public final static DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    public static void main(String[] args) {
        ctx = SpringApplication.run(Main.class, args);
        System.out.println("Hello world!");
    }

    public static boolean excp(Exception e) {
        if(debug) {
            e.printStackTrace();
        } else {
            System.out.println(e.getMessage());
        }
        return false;
    }
}