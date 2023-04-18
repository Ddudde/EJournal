package ru.mirea.data.reps.school.dayOfWeek;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.dayOfWeek.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByNameAndSchool(String name, Long school);

    List<Subject> findBySchool(Long school);
}