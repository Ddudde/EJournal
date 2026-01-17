package ru.services.interfaces.logic.people;

import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DTO.controller.people.admin.AdminsOutDTO;

public interface IAdminsService {
    AdminsOutDTO deleteRoleUser(User user1, Syst syst);

    AdminsOutDTO changeFIO(User user1, String name);

    AdminsOutDTO addNewAccountWithRole(Syst syst, String name);
}
