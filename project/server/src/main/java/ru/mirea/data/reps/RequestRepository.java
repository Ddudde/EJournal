package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}