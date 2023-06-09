package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.Period;
import ru.mirea.data.models.school.School;

public interface PeriodRepository extends JpaRepository<Period, Long> {
}