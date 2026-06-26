package ru.data.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.Contacts;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Long> {
}