package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Lesson;
import ru.mirea.data.models.Mark;

public interface MarkRepository extends JpaRepository<Mark, Long> {
}