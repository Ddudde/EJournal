package ru.services.interfaces.logic.people;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.people.hTeacher.HTeachersBodyDTO;
import ru.data.DTO.controller.people.hTeacher.HTeachersOutDTO;

import java.util.List;
import java.util.Map;

public interface IHTeachersService {
    HTeachersOutDTO removeGroup(Long grId, School school);

    HTeachersOutDTO addGroup(String name, School school);

    HTeachersOutDTO changeNameGroup(String name, Group group);

    HTeachersOutDTO changeFIO(User user1, String name, Long schoolId);

    HTeachersOutDTO deleteRoleUser(User user1, School school);

    HTeachersOutDTO addNewAccountWithRole(String name, School sch);

    HTeachersOutDTO changeNameSchool(String name, School school);

    HTeachersOutDTO addSchool(String name);

    HTeachersOutDTO deleteSchool(School school, Long schId);

    Map<Long, HTeachersBodyDTO> prepareInfoForAdmins(List<School> schools);
}
