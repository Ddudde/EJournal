package ru.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.controllers.SSE.TypesConnect;
import ru.data.DAO.Contacts;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.contact.ContactOutDTO;
import ru.data.DTO.controller.contact.ContactsInnerDTO;
import ru.security.user.CustomToken;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.IContactService;
import ru.services.interfaces.logic.ISSEService;

import java.util.Objects;

/** RU: Контроллер для раздела контактов + Server Sent Events
 * <pre>
 * Swagger: <a href="http://localhost:9001/EJournal/swagger/htmlSwag/#/ContactsController">http://localhost:9001/swagger/htmlSwag/#/ContactsController</a>
 * </pre> */
@RequiredArgsConstructor
@RestController
@RequestMapping("/contacts/")
public class ContactController {
    private final IDBService dbService;
    private final IContactService contactService;
    private final ISSEService sseService;

    /** RU: изменение контакта + Server Sent Events
     * @see DocsHelpController#point Описание */
    @PreAuthorize("@code401.check(@dbService.existUserBySubscription(#sub))")
    @PutMapping("/chContact/")
    public ResponseEntity<Void> chContact(@RequestBody ContactsInnerDTO body, @AuthenticationPrincipal SubscriberDTO sub) {
        final User user = dbService.userById(sub.getUserId());
        Contacts contacts = contactService.prepareContactsForChangeContact(sub, user);
        if(contacts == null) return ResponseEntity.notFound().build();

        final ContactOutDTO outDTO = contactService.changeContact(body, contacts);
        sseService.sendEventFor("chContactC", outDTO, TypesConnect.CONTACTS, sub.getLvlSch(), "main", "main", sub.getLvlMore2());
        return ResponseEntity.ok().build();
    }

    /** RU: [start] Отправка контактов, портала/школы
     * @param type Нужный тип: Por - портал, Yo - школы
     * @see DocsHelpController#point Описание */
    @GetMapping("/getContacts/{type}/")
    public ResponseEntity<ContactOutDTO> getContacts(@PathVariable String type, @AuthenticationPrincipal SubscriberDTO sub, CustomToken auth) {
        final User user = dbService.userById(sub.getUserId());
        final Syst syst = dbService.getSyst();
        Long schId = null;
        Contacts contacts = null;
        if (Objects.equals(type, "Yo") && user != null) {
            final School school = user.getSelecRole().getYO();
            schId = school.getId();
            if(school != null) contacts = school.getContacts();
        }
        if (Objects.equals(type, "Por") && syst != null) {
            contacts = syst.getContacts();
        }
        if(contacts == null) return ResponseEntity.notFound().build();

        final ContactOutDTO outDTO = contactService.prepareContact(contacts);
        sseService.changeSubscriber(auth.getUUID(), null, TypesConnect.CONTACTS, schId + "", "main", "main", type);
        return ResponseEntity.ok(outDTO);
    }

}