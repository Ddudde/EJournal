package ru.controllers;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.data.DAO.Contacts;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.SSE.Subscriber;
import ru.data.SSE.TypesConnect;
import ru.security.user.CustomToken;
import ru.security.user.Roles;

import java.util.Objects;

import static ru.Main.datas;

/** RU: Контроллер для раздела контактов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ContactsController">http://localhost:9001/swagger/htmlSwag/#/ContactsController</a>
 * </pre> */
@RequestMapping("/contacts")
@RequiredArgsConstructor
@RestController public class ContactsController {

    /** RU: изменение контакта + Server Sent Events
     * @see DocsHelpController#point(Object, Object) Описание */
    @PreAuthorize("@code401.check(#sub.getUser() != null)")
    @PutMapping("/chContact")
    public ResponseEntity<Void> chContact(@RequestBody DataContacts body, @AuthenticationPrincipal Subscriber sub) throws Exception {
        final User user = sub.getUser();
        final Syst syst = datas.getDbService().getSyst();
        Contacts contacts = null;
        final JsonTreeWriter wrtr = datas.init(body.toString(), "[PUT] /chContact");
        HttpStatus stat = HttpStatus.NOT_FOUND;
        if(user.getRoles().containsKey(Roles.ADMIN) && Objects.equals(sub.getLvlMore2(), "Por")){
            contacts = syst.getContacts();
        }
        if(user.getRoles().containsKey(Roles.HTEACHER) && Objects.equals(sub.getLvlMore2(), "Yo")){
            final School school = user.getSelecRole().getYO();
            if(school != null) contacts = school.getContacts();
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
            SSEController.sendEventFor("chContactC", ans, TypesConnect.CONTACTS, sub.getLvlSch(), "main", "main", sub.getLvlMore2());
        }, wrtr, stat);
    }

    /** RU: [start] Отправка контактов, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @see DocsHelpController#point(Object, Object) Описание */
    @GetMapping("/getContacts/{type}")
    public ResponseEntity<JsonObject> getContacts(@PathVariable String type, @AuthenticationPrincipal Subscriber sub, CustomToken auth) throws Exception {
        final User user = sub.getUser();
        final JsonTreeWriter wrtr = datas.init("type= "+type, "[GET] /getContacts");
        final Syst syst = datas.getDbService().getSyst();
        Contacts contacts = null;
        HttpStatus stat = HttpStatus.NOT_FOUND;
        final var ref = new Object() {
            Long schId = null;
        };
        if (Objects.equals(type, "Yo") && user != null) {
            final School school = user.getSelecRole().getYO();
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
            SSEController.changeSubscriber(auth.getUUID(), sub.getLogin(), TypesConnect.CONTACTS, ref.schId + "", "main", "main", type);
        }, wrtr, stat, false);
    }

    /** RU: Данные клиента используемые ContactsController в методах
     * @see ContactsController */
    @ToString
    @RequiredArgsConstructor
    static final class DataContacts {
        public final String p, p1, val;
    }
}