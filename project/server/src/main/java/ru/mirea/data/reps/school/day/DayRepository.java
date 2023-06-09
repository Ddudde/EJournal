package ru.mirea.data.reps.school.day;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.data.models.school.day.Day;

import java.util.List;

public interface DayRepository extends JpaRepository<Day, Long> {
    List<Day> findBySchoolAndTeacherAndGrpAndNameSubject(Long school, Long teacher, Long grp, String nameSubject);

    @Query("SELECT DISTINCT d.dat, d.marks FROM daay d WHERE (d.school = :school) AND (d.teacher = :teacher) AND (d.grp = :grp) AND (d.nameSubject = :nameSubject)")
    List<Object[]> uniqDatAndMarksByParams(@Param("school") Long school, @Param("teacher") Long teacher, @Param("grp") Long grp, @Param("nameSubject") String nameSubject);
}