package ru.services.interfaces.logic.school;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.teacherJournal.TeacherJournalInnerDTO;
import ru.data.DTO.controller.school.teacherJournal.TeacherJournalOutDTO;
import ru.data.DTO.service.data.GroupServiceDTO;

public interface ITeacherJournalService {
    TeacherJournalOutDTO addHomework(TeacherJournalInnerDTO body, SubscriberDTO sub, User user, Group group, School school);

    TeacherJournalOutDTO addMark(TeacherJournalInnerDTO body, SubscriberDTO sub, School school, User user, Group group, User objU);

    TeacherJournalOutDTO prepareMarksAndHomeworksForGroup(SubscriberDTO sub, School school, User user, Group group);

    GroupServiceDTO groupsByList(Long schoolId, String nameSubject, Long userId);

    TeacherJournalOutDTO prepareScheduleAndPeriods(School school, User user);
}
