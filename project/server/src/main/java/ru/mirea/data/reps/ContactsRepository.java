package ru.mirea.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.Contacts;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
}