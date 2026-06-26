package ru.services.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.people.StudentsController;
import ru.controllers.school.TeacherJournalController;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.security.user.AuthToken;
import ru.services.interfaces.data.IGroupService;
import ru.services.interfaces.db.IDBService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RU: Сервис для сущности Group
 * @see Group*/
@Service
@RequiredArgsConstructor
public class GroupService implements IGroupService {
    private final IDBService dbService;

    /** RU: готовит JSON с данными групп школы.
     * <pre>
     * bodyG : {
     *     group.ID : group.name
     * }
     * </pre>
     * @return ID первой группы
     * @see StudentsController#getInfo(AuthToken)   Пример использования */
    @Override
    public GroupServiceDTO groupsBySchoolOfUser(User user) {
        final Map<Long, String> bodyGroup = new HashMap<>();
        Long first = null;
        if (user == null) return null;

        final School school = dbService.getFirstRole(user.getRoles()).getYO();
        final List<Group> groups = school.getGroups();

        if (!ObjectUtils.isEmpty(groups)) {
            first = groups.getFirst().getId();
            for (Group gr : groups) {
                bodyGroup.put(gr.getId(), gr.getName());
            }
        }
        return new GroupServiceDTO(first, bodyGroup);
    }

    /** RU: готовит JSON с данными групп школы.
     * <pre>
     * bodyG : {
     *     group.ID : group.name
     * }
     * </pre>
     * @return ID первой группы
     * @see TeacherJournalController#getInfoPart2(AuthToken, String)    Пример использования */
    @Override
    public GroupServiceDTO groupsByList(List<Long> groupsId) {
        final Map<Long, String> bodyGroup = new HashMap<>();
        if (ObjectUtils.isEmpty(groupsId)) return null;

        final Long first = groupsId.getFirst();
        for (Long groupId : groupsId) {
            final Group gr = dbService.groupById(groupId);
            bodyGroup.put(groupId, gr.getName());
        }
        return new GroupServiceDTO(first, bodyGroup);
    }
}
