package ru.controllers.school;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.controllers.AuthController;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.data.models.auth.User;
import ru.data.models.school.Request;
import ru.security.CustomToken;

import static ru.Main.datas;

/** RU: Контроллер для раздела заявок на подключение к системе учебных учреждений + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/RequestController">http://localhost:9001/swagger/htmlSwag/#/RequestController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  - Тестирование
 *  - Swagger
 * </pre>
 * @see Subscriber */
@RequestMapping("/requests")
@RequiredArgsConstructor
@RestController public class RequestController {
    
    private final AuthController authController;

    /** RU: добавляет заявку + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: date, email, fio
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PostMapping("/addReq")
    public ResponseEntity<Void> addReq(@RequestBody DataRequest body) throws Exception {
        JsonTreeWriter wrtr = datas.init(body.toString(), "[POST] /addReq");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(!ObjectUtils.isEmpty(body.email) && !ObjectUtils.isEmpty(body.date) && !ObjectUtils.isEmpty(body.fio)) {
            Request request = new Request(body.email, body.date, body.fio);
            datas.getDbService().getRequestRepository().saveAndFlush(request);

            wrtr.name("id").value(request.getId())
                .name("body").beginObject()
                .name("title").value(request.getEmail())
                .name("date").value(request.getDate())
                .name("text").value(request.getFio())
                .endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("addReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: удаление заявки + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, title
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @DeleteMapping("/delReq")
    public ResponseEntity<Void> delReq(@RequestBody DataRequest body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[DELETE] /delReq");
        Request request = datas.getDbService().requestById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getRoles().containsKey(4L) && request != null) {
            datas.getDbService().getRequestRepository().delete(request);

            wrtr.name("id").value(request.getId());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("delReq", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: изменение заголовка заявки + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, title
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chTitle")
    public ResponseEntity<Void> chTitle(@RequestBody DataRequest body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chTitle");
        Request request = datas.getDbService().requestById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getRoles().containsKey(4L) && request != null) {
            request.setEmail(body.title);
            datas.getDbService().getRequestRepository().saveAndFlush(request);

            wrtr.name("id").value(request.getId())
                .name("title").value(request.getEmail());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chTitle", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: изменение даты заявки + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, date
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chDate")
    public ResponseEntity<Void> chDate(@RequestBody DataRequest body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chDate");
        Request request = datas.getDbService().requestById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getRoles().containsKey(4L) && request != null) {
            request.setDate(body.date);
            datas.getDbService().getRequestRepository().saveAndFlush(request);

            wrtr.name("id").value(request.getId())
                .name("date").value(request.getDate());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chDate", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: изменение текста заявки + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: id, text
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chText")
    public ResponseEntity<Void> chText(@RequestBody DataRequest body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chText");
        Request request = datas.getDbService().requestById(body.id);
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getRoles().containsKey(4L) && request != null) {
            request.setFio(body.text);
            datas.getDbService().getRequestRepository().saveAndFlush(request);

            wrtr.name("id").value(request.getId())
                .name("text").value(request.getFio());
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chText", ans, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat);
    }

    /** RU: [start] отправляет инфу о заявках
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getRequests")
    public ResponseEntity<JsonObject> getRequests(CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init("", "[GET] /getRequests");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null && user.getRoles().containsKey(4L)) {
            for(Request reqR : datas.getDbService().getRequests()){
                wrtr.name(reqR.getId()+"").beginObject()
                    .name("title").value(reqR.getEmail())
                    .name("date").value(reqR.getDate())
                    .name("text").value(reqR.getFio())
                    .endObject();
            }
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), null, TypesConnect.REQUESTS, "main", "main", "main", "main");
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые RequestController в методах
     * @see RequestController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataRequest {
        public String text, date, title, email, fio;
        public Long id;
    }
}