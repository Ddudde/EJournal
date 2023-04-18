package ru.mirea.data.reps.school.dayOfWeek;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.dayOfWeek.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}