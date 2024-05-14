package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.school.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}