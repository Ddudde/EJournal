package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.data.DAO.school.Day;

import java.util.List;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

    Day findBySchoolIdAndTeacherIdAndGrpIdAndNameSubjectAndDat(Long school, Long teacher, Long grp, String nameSubject, String dat);

    List<Day> findBySchoolIdAndTeacherIdAndGrpIdAndNameSubject(Long school, Long teacher, Long grp, String nameSubject);

    @Query("SELECT DISTINCT d.nameSubject, d.dat, d.homework FROM daay d WHERE (d.school.id = :school) AND (d.grp.id = :grp)")
    List<Object[]> uniqNameSubAndDatAndHomeworkByParams(@Param("school") Long school, @Param("grp") Long grp);

    @Query("SELECT DISTINCT d.dat, d.homework FROM daay d WHERE (d.school.id = :school) AND (d.grp.id = :grp) AND (d.nameSubject = :nameSubject)")
    List<Object[]> uniqDatAndHomeworkByParams(@Param("school") Long school, @Param("grp") Long grp, @Param("nameSubject") String nameSubject);

    @Query("SELECT DISTINCT d.nameSubject, d.dat, mar FROM daay d JOIN d.marks mar WHERE (d.school.id = :school) AND (d.grp.id = :grp) AND (mar.period.id = :period)")
    List<Object[]> uniqNameSubjectAndDatAndMarksByParams(@Param("school") Long school, @Param("grp") Long grp, @Param("period") Long period);

    @Query("SELECT DISTINCT d.dat, mar.id FROM daay d JOIN d.marks mar WHERE (d.school.id = :school) AND (d.teacher.id = :teacher) AND (d.grp.id = :grp) AND (d.nameSubject = :nameSubject)")
    List<Object[]> uniqDatAndMarksByParams(@Param("school") Long school, @Param("teacher") Long teacher, @Param("grp") Long grp, @Param("nameSubject") String nameSubject);

}