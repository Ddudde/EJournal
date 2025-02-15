package ru.data.reps.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.data.DAO.school.School;

import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("SELECT per.name FROM School s JOIN s.periods per WHERE (s.id = :school)")
    List<String> uniqPeriodsById(@Param("school") Long school);
}