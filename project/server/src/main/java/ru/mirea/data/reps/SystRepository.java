package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Syst;

public interface SystRepository extends JpaRepository<Syst, Long> {
}