package ru.services.logic;

import ru.data.DAO.Contacts;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.contact.ContactOutDTO;
import ru.data.DTO.controller.contact.ContactsInnerDTO;

public interface IContactService {
    ContactOutDTO changeContact(ContactsInnerDTO body, Contacts contacts);

    Contacts prepareContactsForChangeContact(SubscriberDTO sub, User user);

    ContactOutDTO prepareContact(Contacts contacts);
}
