package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.auth.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String login);

    User findByCode(String code);
}