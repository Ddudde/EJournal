package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.School;

public interface SchoolRepository extends JpaRepository<School, Long> {
}