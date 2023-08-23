package ru.mirea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
public class Main {

    public static ConfigurableApplicationContext ctx;

    public static boolean debug = true;

    public static boolean test = true;

    public final static DateFormat df = new SimpleDateFormat("dd.MM.yy");

    public static void main(String[] args) {
        ctx = SpringApplication.run(Main.class, args);
        System.out.println("Hello world!");
        ServerService serverService = (ServerService) ctx.getBean("serverService");
        User user = serverService.getDbService().userByLogin("nm12");
        System.out.println(user);
        School school = serverService.getDbService().schoolById(26L);
        System.out.println(school);
//        serverService.getDbService().getDayRepository().uniqDatAndMarksByParams();
//        serverService.getEmailService().sendRegCode("myratoff25@gmail.com");
//        serverService.sendSimpleMessage("myratoff25@gmail.com", "TestG2", "test");
//        serverService.sendSimpleMessage("nat.muradov@yandex.com", "TestY2", "test");
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