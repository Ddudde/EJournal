package ru.mirea.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mirea.data.models.auth.User;

import static ru.mirea.Main.datas;

@Service
public class UserService implements UserDetailsService {

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User myUser = datas.getDbService().userByLogin(username);
//        if (myUser == null) {
//            throw new UsernameNotFoundException("Unknown user: " + username);
//        }
        return myUser;
    }
}
