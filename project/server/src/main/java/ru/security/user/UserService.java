package ru.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.data.DAO.auth.User;
import ru.services.db.DBService;

@RequiredArgsConstructor
@Service public class UserService implements UserDetailsService {
    private final DBService dbService;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final User myUser = dbService.userByLogin(username);
//        if (myUser == null) {
//            throw new UsernameNotFoundException("Unknown user: " + username);
//        }
        return myUser;
    }
}
