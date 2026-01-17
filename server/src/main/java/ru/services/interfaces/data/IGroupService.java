package ru.services.interfaces.data;

import ru.data.DAO.auth.User;
import ru.data.DTO.service.data.GroupServiceDTO;

import java.util.List;

public interface IGroupService {
    GroupServiceDTO groupsBySchoolOfUser(User user);

    GroupServiceDTO groupsByList(List<Long> groupsId);
}
