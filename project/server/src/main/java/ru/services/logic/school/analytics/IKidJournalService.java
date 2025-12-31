package ru.services.logic.school.analytics;

import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.school.analytics.kidJournal.KidJournalOutDTO;

import java.util.List;

public interface IKidJournalService {
    KidJournalOutDTO preparePeriodMarks(List<Period> periods, User user, School sch, Group group);

    KidJournalOutDTO prepareInfo(School sch, Group group);
}
