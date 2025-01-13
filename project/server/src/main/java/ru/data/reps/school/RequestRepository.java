package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.DAO.school.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}