package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.data.models.school.School;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("SELECT per.name FROM School s JOIN s.periods per WHERE (s.id = :school)")
    List<String> uniqPeriodsById(@Param("school") Long school);
}