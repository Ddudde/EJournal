package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.auth.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String login);

    User findByCode(String code);
}