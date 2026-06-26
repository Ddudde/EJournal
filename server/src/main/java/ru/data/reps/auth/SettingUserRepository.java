package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.auth.SettingUser;

@Repository
public interface SettingUserRepository extends JpaRepository<SettingUser, Long> {
}