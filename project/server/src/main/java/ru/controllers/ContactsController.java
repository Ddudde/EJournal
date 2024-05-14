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
import ru.data.SSE.TypesConnect;
import ru.data.models.Contacts;
import ru.data.models.Syst;
import ru.data.models.auth.User;
import ru.data.models.school.School;
import ru.security.CustomToken;

import java.util.Objects;

import static ru.Main.datas;

/** RU: Контроллер для раздела контактов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/swagger/htmlSwag/#/ContactsController">http://localhost:9001/swagger/htmlSwag/#/ContactsController</a>
 * beenDo: Сделано
 *  + Javadoc
 *  + Security
 *  + Переписка
 *  + Переписка2
 *  + Тестирование
 *  + Swagger
 * </pre> */
@RequestMapping("/contacts")
@RequiredArgsConstructor
@RestController public class ContactsController {

    private final AuthController authController;

    /** RU: изменение контакта + Server Sent Events
     * @param body Данные с клиента, задействуются свойства: p, p1, val
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Код статуса */
    @PatchMapping("/chContact")
    public ResponseEntity<Void> chContact(@RequestBody DataContacts body, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        Syst syst = datas.getDbService().getSyst();
        Contacts contacts = null;
        JsonTreeWriter wrtr = datas.init(body.toString(), "[PATCH] /chContact");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user != null) {
            if(user.getRoles().containsKey(4L) && Objects.equals(auth.getSub().getLvlMore2(), "Por")){
                contacts = syst.getContacts();
            }
            if(user.getRoles().containsKey(3L) && Objects.equals(auth.getSub().getLvlMore2(), "Yo")){
                School school = user.getRoles().get(user.getSelRole()).getYO();
                if(school != null) contacts = school.getContacts();
            }
        }
        if(contacts != null) {
            if(Objects.equals(body.p, "contact")) {
                contacts.setContact(body.val);
            }
            if(Objects.equals(body.p, "mapPr")) {
                if(Objects.equals(body.p1, "text")) {
                    contacts.setText(body.val);
                }
                if(Objects.equals(body.p1, "imgUrl")) {
                    contacts.setImgUrl(body.val);
                }
            }
            datas.getDbService().getContactsRepository().saveAndFlush(contacts);
            wrtr.name("val").value(body.val)
                .name("p").value(body.p)
                .name("p1").value(body.p1);
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.sendEventFor("chContactC", ans, TypesConnect.CONTACTS, auth.getSub().getLvlSch(), "main", "main", auth.getSub().getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: [start] Отправка контактов, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @param auth Авторизация, в ней подписка и пользователь
     * @exception Exception Исключение вызывается при ошибках с Json
     * @return Объект и код статуса */
    @GetMapping("/getContacts/{type}")
    public ResponseEntity<JsonObject> getContacts(@PathVariable String type, CustomToken auth) throws Exception {
        User user = auth.getSub().getUser();
        JsonTreeWriter wrtr = datas.init("type= "+type, "[GET] /getContacts");
        Syst syst = datas.getDbService().getSyst();
        Contacts contacts = null;
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            Long schId = null;
        };
        if (Objects.equals(type, "Yo") && user != null) {
            School school = user.getSelecRole().getYO();
            ref.schId = school.getId();
            if(school != null) contacts = school.getContacts();
        }
        if (Objects.equals(type, "Por") && syst != null) {
            contacts = syst.getContacts();
        }
        if(contacts != null) {
            wrtr.name("contact").value(contacts.getContact())
                .name("mapPr").beginObject()
                .name("text").value(contacts.getText())
                .name("imgUrl").value(contacts.getImgUrl())
                .endObject();
            stat = HttpStatus.OK;
        }
        return datas.getObjR(ans -> {
            authController.infCon(auth.getUUID(), auth.getSub().getLogin(), TypesConnect.CONTACTS, ref.schId + "", "main", "main", type);
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые ContactsController в методах
     * @see ContactsController */
    @ToString
    @NoArgsConstructor @AllArgsConstructor
    static class DataContacts {
        public String p, p1, val;
    }
}