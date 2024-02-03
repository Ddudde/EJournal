package ru.mirea.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mirea.data.models.auth.User;
import ru.mirea.services.ServerService;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private ServerService datas;
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User myUser = datas.getDbService().userByLogin(username);
        if (myUser == null) {
            throw new UsernameNotFoundException("Unknown user: " + username);
        }
        return myUser;
    }
}
