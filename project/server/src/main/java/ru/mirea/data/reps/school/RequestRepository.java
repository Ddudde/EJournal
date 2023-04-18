package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}