package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.DAO.school.Period;

public interface PeriodRepository extends JpaRepository<Period, Long> {
}