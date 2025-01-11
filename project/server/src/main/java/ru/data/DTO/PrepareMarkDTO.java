package ru.data.DTO;

import lombok.NoArgsConstructor;
import ru.data.models.school.Day;
import ru.data.models.school.Mark;
import ru.data.models.school.Period;

@NoArgsConstructor
public class PrepareMarkDTO {
    public Mark mark;
    public Day day;
    public Period period;
    public boolean oldMark;
}
