package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}