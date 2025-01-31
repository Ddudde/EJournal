package ru.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.data.DAO.auth.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}