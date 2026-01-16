package ru.services.logic.people;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.people.teacher.TeacherOutDTO;
import ru.data.DTO.service.school.TeacherServiceDTO;

public interface ITeacherService {
    TeacherOutDTO deleteRoleUser(User user1, Group group);

    TeacherOutDTO changeFIO(User user1, String name);

    TeacherOutDTO addNewAccountWithRole(School school, String name);

    @SuppressWarnings("JavadocReference")
    TeacherServiceDTO teachersBySchool(School school);
}
