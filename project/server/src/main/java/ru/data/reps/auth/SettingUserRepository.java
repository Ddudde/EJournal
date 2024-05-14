package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.auth.SettingUser;

public interface SettingUserRepository extends JpaRepository<SettingUser, Long> {
}