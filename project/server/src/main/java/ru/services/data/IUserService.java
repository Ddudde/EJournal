package ru.services.data;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.service.data.UserServiceDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;

import java.util.List;
import java.util.Map;

public interface IUserService {
    User loadUserByUsername(String username) throws UsernameNotFoundException;

    UserServiceDTO setKidIDOfParent(User user, Long idL);

    UserServiceDTO setNextRole(User user);

    UserServiceDTO changeEmail(User user, String email);

    UserServiceDTO changeProfileInfo(SettingUser settingUser, String info);

    UserServiceDTO changeLogin(User user, String newLogin, SubscriberDTO sub);

    Map<Long, UserServiceBodyUserDTO> usersByListEntity(List<User> list, boolean lin);

    Map<Long, UserServiceBodyUserDTO> usersByListId(List<Long> list, boolean lin);
}
