package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.data.models.auth.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}