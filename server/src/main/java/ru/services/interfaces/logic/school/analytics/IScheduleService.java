package ru.services.interfaces.logic.school.analytics;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleInnerDTO;
import ru.data.DTO.controller.school.analytics.schedule.ScheduleOutDTO;
import ru.data.DTO.service.school.ScheduleServiceDTO;

public interface IScheduleService {
    ScheduleOutDTO addLesson(ScheduleInnerDTO body, User teaU, School school, Group group);

    ScheduleServiceDTO getShedule(User user, Long groupId);

    ScheduleOutDTO prepareInfoForHTeacherOrTEACHER(User user, School school);
}
