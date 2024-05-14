package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.Syst;

public interface SystRepository extends JpaRepository<Syst, Long> {
}