package ru.mirea.data.reps.school.day;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.school.Lesson;
import ru.mirea.data.models.school.day.Mark;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByUserInvAndWeight(Long userInv, int weight);

    List<Mark> findByIdInAndUserInv(List<Long> id, Long userInv);

    List<Mark> findByUsrAndWeight(Long usr, int weight);

    List<Mark> findByIdInAndUsr(List<Long> id, Long usr);
}