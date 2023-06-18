package ru.mirea.data.reps.school.day;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.day.Mark;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByUsrAndWeight(Long usr, int weight);

    List<Mark> findByIdInAndUsr(List<Long> id, Long usr);
}