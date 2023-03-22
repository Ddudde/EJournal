package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Day;
import ru.mirea.data.models.News;

public interface DayRepository extends JpaRepository<Day, Long> {
}