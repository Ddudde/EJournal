package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.DayOfWeek;

public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Long> {
}