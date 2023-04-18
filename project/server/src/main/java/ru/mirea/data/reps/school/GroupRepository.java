package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}