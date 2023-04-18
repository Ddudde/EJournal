package ru.mirea.data.reps.school.dayOfWeek;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.dayOfWeek.DayOfWeek;

public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Long> {
}