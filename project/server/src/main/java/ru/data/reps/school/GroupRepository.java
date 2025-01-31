package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.school.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}