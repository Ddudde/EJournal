package ru.services.data;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.service.data.UserServiceDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyEmailDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyLoginDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyProfileInfoDTO;
import ru.data.DTO.service.data.userBody.UserServiceBodyUserDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.user.Roles;
import ru.services.interfaces.data.IUserService;
import ru.services.interfaces.db.IDBService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RU: Сервис для сущности User
 * @see User */
@RequiredArgsConstructor
@Service public class UserService implements UserDetailsService, IUserService {
    private final IDBService dbService;
    private final UserRepository userRepository;
    private final SettingUserRepository settingUserRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final User myUser = dbService.userByLogin(username);
//        if (myUser == null) {
//            throw new UsernameNotFoundException("Unknown user: " + username);
//        }
        return myUser;
    }

    @Override
    public UserServiceDTO setKidIDOfParent(User user, Long idL) {
        final UserServiceDTO.UserServiceDTOBuilder builderDTO = UserServiceDTO.builder();
        user.setSelKid(idL);
        userRepository.saveAndFlush(user);
        builderDTO.kid(user.getSelKid());
        return builderDTO.build();
    }

    @Override
    public UserServiceDTO setNextRole(User user) {
        final Roles curRol = user.getSelRole();
        final UserServiceDTO.UserServiceDTOBuilder builderDTO = UserServiceDTO.builder();
        int role = curRol.i+1;
        Roles roleI;

        while(true){
            if(role == 5) role = 0;
            roleI = Roles.roleByInteger(role);
            if(!user.getRoles().containsKey(roleI)) {
                role++;
                continue;
            }
            break;
        }
        builderDTO.role(role);
        user.setSelRole(roleI);
        userRepository.saveAndFlush(user);
        final Role roleEntity = user.getRoles().get(roleI);
        if(roleI == Roles.PARENT) {
            final Map<String, String> kids = new HashMap<>();
            if (!ObjectUtils.isEmpty(roleEntity.getKids())) {
                for (User kid : roleEntity.getKids()) {
                    kids.put(kid.getId() + "", kid.getFio());
                }
            }
            builderDTO.kid(user.getSelKid())
                .kids(kids);
        }
        return builderDTO.build();
    }

    @Override
    public UserServiceDTO changeEmail(User user, String email) {
        final UserServiceDTO.UserServiceDTOBuilder builderDTO = UserServiceDTO.builder();

        user.getSelecRole().setEmail(email);
        userRepository.saveAndFlush(user);
        final UserServiceBodyEmailDTO bodyEmailDTO = new UserServiceBodyEmailDTO(email, user.getSelRole().i);
        builderDTO.body(bodyEmailDTO);
        return builderDTO.build();
    }

    @Override
    public UserServiceDTO changeProfileInfo(SettingUser settingUser, String info) {
        final UserServiceDTO.UserServiceDTOBuilder builderDTO = UserServiceDTO.builder();

        settingUser.setInfo(info);
        settingUserRepository.saveAndFlush(settingUser);
        final UserServiceBodyProfileInfoDTO bodyProfileInfoDTO = new UserServiceBodyProfileInfoDTO(settingUser.getInfo());
        builderDTO.body(bodyProfileInfoDTO);
        return builderDTO.build();
    }

    @Override
    public UserServiceDTO changeLogin(User user, String newLogin, SubscriberDTO sub) {
        final UserServiceDTO.UserServiceDTOBuilder builderDTO = UserServiceDTO.builder();

        user.setUsername(newLogin);
        userRepository.saveAndFlush(user);
        final UserServiceBodyLoginDTO bodyLoginDTO = new UserServiceBodyLoginDTO(sub.getLvlMore2(), user.getUsername());
        sub.setLvlMore2(newLogin);
        builderDTO.body(bodyLoginDTO);
        return builderDTO.build();
    }

    /** RU: готовит JSON с данными списка пользователей.
     * <pre>
     * user.ID : {
     *     "name" : "FIO",
     *     "login",
     *     "link",
     * }
     * </pre>
     * @param lin добавляет свойство "link" в объект */
    @Override
    public Map<Long, UserServiceBodyUserDTO> usersByListEntity(List<User> list, boolean lin) {
        if (list == null || ObjectUtils.isEmpty(list)) return null;
        final Map<Long, UserServiceBodyUserDTO> bodysUserDTO = new HashMap<>();

        for (User objU : list) {
            if (objU == null) continue;

            prepareUserForList(lin, bodysUserDTO, objU);
        }
        return bodysUserDTO;
    }

    /** RU: готовит JSON с данными списка пользователей.
     * <pre>
     * user.ID : {
     *     "name" : "FIO",
     *     "login",
     *     "link",
     * }
     * </pre>
     * @param lin добавляет свойство "link" в объект */
    @Override
    public Map<Long, UserServiceBodyUserDTO> usersByListId(List<Long> list, boolean lin) {
        if (list == null || ObjectUtils.isEmpty(list)) return null;
        final Map<Long, UserServiceBodyUserDTO> bodysUserDTO = new HashMap<>();

        for (Long id : list) {
            User objU = dbService.userById(id);
            if (objU == null) continue;

            prepareUserForList(lin, bodysUserDTO, objU);
        }
        return bodysUserDTO;
    }

    private void prepareUserForList(boolean lin, Map<Long, UserServiceBodyUserDTO> bodysUserDTO, User objU) {
        final UserServiceBodyUserDTO.UserServiceBodyUserDTOBuilder bodyUserDTOBuilder = UserServiceBodyUserDTO.builder();
        bodyUserDTOBuilder.name(objU.getFio());
        if (!ObjectUtils.isEmpty(objU.getUsername())) {
            bodyUserDTOBuilder.login(objU.getUsername());
        }
        if (lin && !ObjectUtils.isEmpty(objU.getCode())) {
            bodyUserDTOBuilder.link(objU.getCode());
        }
        bodysUserDTO.put(objU.getId(), bodyUserDTOBuilder.build());
    }

    /** @return В оригинале рандомится с нижним подчёркиванием, заменяем на дефис*/
    public static String getRandomUsername(Faker faker){
        return faker.internet().slug().replace('_', '-');
    }
}
