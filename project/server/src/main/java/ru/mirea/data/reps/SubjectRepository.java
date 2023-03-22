package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.yaml.snakeyaml.error.Mark;
import ru.mirea.data.models.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByNameAndSchool(String name, Long school);

    List<Subject> findBySchool(Long school);
}