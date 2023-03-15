package ru.mirea.data.reps.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.data.models.auth.Invite;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    Invite findByCode(String code);
}