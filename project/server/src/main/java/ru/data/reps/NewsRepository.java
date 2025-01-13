package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.DAO.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}