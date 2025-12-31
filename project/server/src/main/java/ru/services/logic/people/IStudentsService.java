package ru.services.logic.people;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DTO.controller.people.students.StudentsOutDTO;

public interface IStudentsService {
    StudentsOutDTO deleteRoleUser(User user1, Group group);

    StudentsOutDTO changeFIO(User user1, String name);

    StudentsOutDTO addNewAccountWithRole(Long schoolId, Group group, String name);
}
