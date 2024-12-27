package ru.security.user;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.data.models.auth.User;

import static ru.Main.datas;

@Service
public class UserService implements UserDetailsService {

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final User myUser = datas.getDbService().userByLogin(username);
//        if (myUser == null) {
//            throw new UsernameNotFoundException("Unknown user: " + username);
//        }
        return myUser;
    }
}
