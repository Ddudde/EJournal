package ru.controllers.people;

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
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.Syst;
import ru.data.models.auth.Role;
import ru.data.models.auth.User;
import ru.security.CustomToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static ru.Main.datas;

/** RU: Контроллер для раздела управления/просмотра администраторов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/AdminsController">http://localhost:9001/swagger/htmlSwag/#/AdminsController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/admins")
@RequiredArgsConstructor
@RestController public class AdminsController {

    private final AuthController authController;

    /** RU: удаляет у пользователя роль администратора + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: name, id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @DeleteMapping("/remPep")
    public ResponseEntity<Void> remPep(@RequestBody DataAdmins body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        User user1 = datas.getDbService().userById(body.id);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /remPep");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        Syst syst = datas.getDbService().getSyst();
        if (user != null && user.getRoles().containsKey(4L) && syst != null && user1 != null) {
            user1.getRoles().remove(4L);
            datas.getDbService().getUserRepository().saveAndFlush(user1);
            syst.getAdmins().remove(user1);
            datas.getDbService().getSystRepository().saveAndFlush(syst);

            wrtr.name("id").value(user1.getId());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("remPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: изменяет фамилию пользователя + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: name, id
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chPep")
    public ResponseEntity<Void> chPep(@RequestBody DataAdmins body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        User user1 = datas.getDbService().userById(body.id);
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chPep");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && user.getRoles().containsKey(4L) && user1 != null) {
            user1.setFio(body.name);
            datas.getDbService().getUserRepository().saveAndFlush(user1);

            wrtr.name("id").value(user1.getId())
                .name("name").value(body.name);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: создаёт пользователя-администратора + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: name
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping("/addPep")
    public ResponseEntity<Void> addPep(@RequestBody DataAdmins body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        Syst syst = datas.getDbService().getSyst();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addPep");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && user.getRoles().containsKey(4L) && syst != null) {
            Instant after = Instant.now().plus(Duration.ofDays(30));
            Date dateAfter = Date.from(after);
            Role role = datas.getDbService().getRoleRepository().saveAndFlush(new Role());
            User inv = new User(body.name, Map.of(
                4L, role
            ), Main.df.format(dateAfter));
            datas.getDbService().getUserRepository().saveAndFlush(inv);
            syst.getAdmins().add(inv);
            datas.getDbService().getSystRepository().saveAndFlush(syst);

            wrtr.name("id").value(inv.getId())
                .name("body").beginObject()
                .name("name").value(body.name)
                .endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("addPepC", ans, TypesConnect.ADMINS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: [start] отправляет список администраторов
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getAdmins")
    public ResponseEntity<JsonObject> getAdmins(CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        Syst syst = datas.getDbService().getSyst();
        JsonTreeWriter wrtr = datas.init("", "[GET] /getAdmins");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if (user != null && syst != null) {
            datas.usersByList(syst.getAdmins(), true, wrtr);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.ADMINS, "null", "main", user.getRoles().containsKey(4L) ? "adm" : "main", "main");
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые AdminsController в методах
     * @see AdminsController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataAdmins {
        public String name;
        public Long id;
    }
}