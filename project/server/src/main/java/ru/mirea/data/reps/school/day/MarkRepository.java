package ru.mirea.data.reps.school.day;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.day.Mark;

public interface MarkRepository extends JpaRepository<Mark, Long> {
}