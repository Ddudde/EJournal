package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.school.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
}