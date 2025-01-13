package ru.data.DTO;

import lombok.NoArgsConstructor;
import ru.data.DAO.school.Day;
import ru.data.DAO.school.Mark;
import ru.data.DAO.school.Period;

@NoArgsConstructor
public class PrepareMarkDTO {
    public Mark mark;
    public Day day;
    public Period period;
    public boolean oldMark;
}
