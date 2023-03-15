package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Group;
import ru.mirea.data.models.Request;

public interface GroupRepository extends JpaRepository<Group, Long> {
}