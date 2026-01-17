package ru.services.interfaces.logic.people;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.people.parents.ParentsInnerDTO;
import ru.data.DTO.controller.people.parents.ParentsOutDTO;

public interface IParentsService {
    ParentsOutDTO deleteRoleUser(User user1, Group group);

    ParentsOutDTO changeFIO(User user1, String name);

    ParentsOutDTO addNewAccountWithRole(User kidU, ParentsInnerDTO body, SubscriberDTO sub);

    ParentsOutDTO prepareParents(Group group);
}
