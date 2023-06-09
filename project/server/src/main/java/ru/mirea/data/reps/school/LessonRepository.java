package ru.mirea.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.data.models.school.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT DISTINCT l.grp FROM Lesson l WHERE (l.school = :school) AND (l.nameSubject = :nameSubject)")
    List<Long> uniqGroupsBySchoolAndSubName(@Param("school") Long school, @Param("nameSubject") String nameSubject);
    @Query("SELECT DISTINCT l.nameSubject FROM Lesson l WHERE l.school = :school")
    List<String> uniqSubNameBySchool(@Param("school") Long school);
    @Query("SELECT DISTINCT l.nameSubject, l.teacher FROM Lesson l WHERE l.school = :school")
    List<Object[]> uniqTeachersBySchool(@Param("school") Long school);

    @Query("SELECT DISTINCT l.nameSubject, l.teacherInv FROM Lesson l WHERE l.school = :school")
    List<Object[]> uniqTeachersInvBySchool(@Param("school") Long school);

    List<Lesson> findBySchoolAndGrp(Long school, Long grp);

    List<Lesson> findBySchoolAndTeacher(Long school, Long teacher);

    List<Lesson> findBySchoolAndGrpAndDayWeek(Long school, Long grp, int dayWeek);
}