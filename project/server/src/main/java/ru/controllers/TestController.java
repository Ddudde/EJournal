package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Main;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.User;
import ru.security.CustomToken;
import ru.services.db.IniDBService;

import static ru.Main.datas;

/** RU: Контроллер для раздела тестирования
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/TestController">http://localhost:9001/swagger/htmlSwag/#/TestController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre> */
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController public class TestController {

    private final AuthController authController;

    private final IniDBService iniDBService;

    /** RU: изменяет параметры тестирования
     * @param body Данные с клиента, задействуются свойства: id, val
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @PatchMapping("/chTests")
    public ResponseEntity<JsonObject> chTests(@RequestBody DataTest body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chBool");
        if(user != null && user.getRoles().containsKey(4L)) {
            switch (body.id) {
                case "checkbox_debug" -> Main.debug = body.val;
                case "checkbox_test" -> {
                    Main.test = body.val;
                    if(Main.test) {
                        iniDBService.testOn();
                    } else {
                        iniDBService.testOff();
                    }
                    iniDBService.getTestInfo(wrtr);
                }
                default -> {}
            }
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {}, wrtr, stat, false);
    }

    /** RU: [start] отправка инфы для тестов
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getInfo")
    public ResponseEntity<JsonObject> getInfo(CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        HttpStatus stat = HttpStatus.NOT_FOUND;
        JsonTreeWriter wrtr = datas.init("", "[GET] /getInfo");
        if(user != null && user.getRoles().containsKey(4L)) {
            wrtr.name("bodyS").beginObject()
                .name("checkbox_debug").value(Main.debug)
                .name("checkbox_test").value(Main.test)
                .endObject();
            iniDBService.getTestInfo(wrtr);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.TEST, "main", "main", "main", "main");
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые TestController в методах
     * @see TestController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataTest {
        public String id;
        public Boolean val;
    }
}