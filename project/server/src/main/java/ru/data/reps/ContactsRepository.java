package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.Contacts;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
}