package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.Syst;

@Repository
public interface SystRepository extends JpaRepository<Syst, Long> {
}