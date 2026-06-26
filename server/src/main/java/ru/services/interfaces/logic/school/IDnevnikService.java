package ru.services.interfaces.logic.school;

import ru.data.DAO.auth.User;
import ru.data.DTO.controller.school.dnevnik.DnevnikOutDTO;

public interface IDnevnikService {
    DnevnikOutDTO prepareDnevnik(User user, Long groupId);
}
