package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.data.models.school.Mark;
import ru.mirea.data.models.school.Period;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByUsrIdAndWeight(Long usr, int weight);

    Mark findByTypeAndStyleAndPeriodIdAndUsrId(String type, String style, Long period, Long usr);

    @Query("SELECT DISTINCT m.style, m FROM Mark m WHERE (m.usr.id = :usr) AND (m.type = :type) AND (m.period IN :periods)")
    List<Object[]> uniqNameSubjectAndMarksByParams(@Param("usr") Long usr, @Param("type") String type, @Param("periods") List<Period> periods);

    List<Mark> findByPeriodInAndTypeAndStyleAndUsrId(List<Period> periods, String type, String style, Long usr);

    List<Mark> findByIdInAndUsrIdAndPeriodId(List<Long> ids, Long usr, Long period);

    List<Mark> findByIdInAndUsrId(List<Long> ids, Long usr);
}