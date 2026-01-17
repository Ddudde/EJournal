package ru.services.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.controllers.ContactController;
import ru.data.DAO.Contacts;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.contact.ContactBodyDTO;
import ru.data.DTO.controller.contact.ContactOutDTO;
import ru.data.DTO.controller.contact.ContactsInnerDTO;
import ru.data.reps.ContactsRepository;
import ru.security.user.Roles;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.IContactService;

import java.util.Map;
import java.util.Objects;

/** RU: сервис для контроллера
 * @see ContactController */
@Service
@RequiredArgsConstructor
public class ContactService implements IContactService {
    private final ContactsRepository contactsRepository;
    private final IDBService dbService;

    @Override
    public ContactOutDTO changeContact(ContactsInnerDTO body, Contacts contacts) {
        final ContactOutDTO.ContactOutDTOBuilder dtoBuilder = ContactOutDTO.builder();

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
        contactsRepository.saveAndFlush(contacts);
        dtoBuilder.val(body.val)
            .p(body.p)
            .p1(body.p1);
        return dtoBuilder.build();
    }

    @Override
    public Contacts prepareContactsForChangeContact(SubscriberDTO sub, User user) {
        final Syst syst = dbService.getSyst();
        Contacts contacts = null;
        final Map<Roles, Role> rolesUser = user.getRoles();
        if(rolesUser.containsKey(Roles.ADMIN) && Objects.equals(sub.getLvlMore2(), "Por")){
            contacts = syst.getContacts();
        }
        if(rolesUser.containsKey(Roles.HTEACHER) && Objects.equals(sub.getLvlMore2(), "Yo")){
            final School school = user.getSelecRole().getYO();
            if(school != null) contacts = school.getContacts();
        }
        return contacts;
    }

    @Override
    public ContactOutDTO prepareContact(Contacts contacts) {
        final ContactOutDTO.ContactOutDTOBuilder dtoBuilder = ContactOutDTO.builder();

        dtoBuilder.contact(contacts.getContact())
            .mapPr(new ContactBodyDTO(contacts.getText(), contacts.getImgUrl()));
        return dtoBuilder.build();
    }
}
