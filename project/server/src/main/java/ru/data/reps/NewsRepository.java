package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
}