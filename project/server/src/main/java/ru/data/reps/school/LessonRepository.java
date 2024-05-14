package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.data.models.auth.User;
import ru.data.models.school.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT DISTINCT l.nameSubject FROM Lesson l WHERE (l.school.id = :school) AND (l.grp.id = :grp)")
    List<String> uniqSubNameBySchoolAndGrp(@Param("school") Long school, @Param("grp") Long grp);
    @Query("SELECT DISTINCT l.grp.id FROM Lesson l WHERE (l.school.id = :school) AND (l.nameSubject = :nameSubject) AND (l.teacher.id = :tea)")
    List<Long> uniqGroupsBySchoolAndSubNameAndTeacher(@Param("school") Long school, @Param("nameSubject") String nameSubject, @Param("tea") Long teacher);
    @Query("SELECT DISTINCT l.nameSubject FROM Lesson l WHERE (l.school.id = :school) AND (l.teacher.id = :tea)")
    List<String> uniqSubNameBySchoolAndTeacher(@Param("school") Long school, @Param("tea") Long teacher);
    @Query("SELECT DISTINCT l.nameSubject, l.teacher.id FROM Lesson l WHERE l.school.id = :school")
    List<Object[]> uniqTeachersLBySchool(@Param("school") Long school);
    @Query("SELECT DISTINCT l.teacher FROM Lesson l WHERE l.school.id = :school")
    List<User> uniqTeachersUBySchool(@Param("school") Long school);

    List<Lesson> findBySchoolIdAndGrpId(Long school, Long grp);

    List<Lesson> findBySchoolIdAndTeacherId(Long school, Long teacher);

    List<Lesson> findBySchoolIdAndGrpIdAndDayWeek(Long school, Long grp, int dayWeek);
}