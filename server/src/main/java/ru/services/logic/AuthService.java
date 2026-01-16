package ru.services.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.controllers.AuthController;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;
import ru.data.DTO.controller.auth.AuthServiceDTO;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.user.Roles;
import ru.services.IPushService;
import ru.services.db.IDBService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** RU: сервис для контроллера
 * @see AuthController */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final IPushService pushService;
    private final SettingUserRepository settingUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IDBService dbService;
    private final UserRepository userRepository;

    @Override
    public AuthOutDTO prepareConnection(AuthInnerDTO body, SubscriberDTO sub, User user) {
        final var dtoBuilder = AuthOutDTO.builder();

        sub.setUserId(user.getId());
        if (!ObjectUtils.isEmpty(body.notifToken)) {
            final SettingUser settingUser = user.getSettings();
            final boolean containsToken = settingUser.getTokens().contains(body.notifToken);
            if(body.permis && !containsToken) {
                pushService.addToken(settingUser, body.notifToken);
                settingUserRepository.saveAndFlush(settingUser);
            }
            if(!body.permis && containsToken){
                pushService.remToken(settingUser, body.notifToken);
                settingUserRepository.saveAndFlush(settingUser);
            }
        }
        final AuthServiceDTO serviceDTO = prepareParentInfo(user);
        dtoBuilder.role(user.getSelRole().i);
        if(serviceDTO != null) {
            dtoBuilder.kid(serviceDTO.kid())
                .kids(serviceDTO.kids());
        }
        return dtoBuilder.build();
    }

    private AuthServiceDTO prepareParentInfo(User user) {
        final var dtoBuilder = AuthServiceDTO.builder();
        final Map<Long, String> kidFIOById = new HashMap<>();
        if (user.getSelRole() != Roles.PARENT) return null;

        final List<User> kids = user.getRoles().get(Roles.PARENT).getKids();
        if (!ObjectUtils.isEmpty(kids)) {
            for (User kid : kids) {
                kidFIOById.put(kid.getId(), kid.getFio());
            }
        }
        dtoBuilder.kid(user.getSelKid())
            .kids(kidFIOById);
        return dtoBuilder.build();
    }

    @Override
    public AuthOutDTO authUser(AuthInnerDTO body, String uuid, User user) {
        final var dtoBuilder = AuthOutDTO.builder();
        final SettingUser settingUser = user.getSettings();
        final Map<Roles, Role> roles = user.getRoles();

        if(!ObjectUtils.isEmpty(body.notifToken)) {
            if(body.permis) {
                pushService.addToken(settingUser, body.notifToken);
            } else {
                pushService.remToken(settingUser, body.notifToken);
            }
            settingUserRepository.saveAndFlush(settingUser);
        }
        final AuthServiceDTO serviceDTO = prepareParentInfo(user);
        dtoBuilder.auth(true)
            .login(user.getUsername())
            .role(user.getSelRole().i)
            .uuidS(uuid)
            .ico(settingUser.getIco())
            .roles(!ObjectUtils.isEmpty(roles) && roles.size() > 1)
            .secFr(!ObjectUtils.isEmpty(settingUser.getSecFr()))
            .email(!ObjectUtils.isEmpty(settingUser.getEmail()));

        if(serviceDTO != null) {
            dtoBuilder.kid(serviceDTO.kid())
                .kids(serviceDTO.kids());
        }
        return dtoBuilder.build();
    }

    /** RU:
     * @return если приглашённый пользователь существует, то возвращается DTO с причиной */
    @Override
    public AuthOutDTO createUser(User invitedUser, AuthInnerDTO body) {
        final var dtoBuilder = AuthOutDTO.builder();
        if(invitedUser == null) {
            dtoBuilder.error("noInv");
            return dtoBuilder.build();
        }

        SettingUser settingUser = invitedUser.getSettings();
        if(Objects.equals(body.mod, "inv")) {
            if(settingUser == null) {
                settingUser = dbService.createSettingUser(new SettingUser(body.ico));
                invitedUser.setSettings(settingUser);
            }
            invitedUser.setSelRole(dbService.getFirstRoleId(invitedUser.getRoles()));
            if(invitedUser.getRoles().containsKey(Roles.PARENT) && !ObjectUtils.isEmpty(invitedUser.getRole(Roles.PARENT).getKids())) {
                invitedUser.setSelKid(invitedUser.getRole(Roles.PARENT).getKids().getFirst().getId());
            }
        } else if(Objects.equals(body.mod, "rea")){
            invitedUser.setCode(null);
            invitedUser.setExpDate(null);
        }
        invitedUser.setUsername(body.login);
        invitedUser.setPassword(passwordEncoder.encode(body.par));
        settingUser.setIco(body.ico);
        userRepository.saveAndFlush(invitedUser);
        if(settingUser != null) {
            final School school = dbService.getFirstRole(invitedUser.getRoles()).getYO();
            if (school != null) {
                pushService.addTopic(settingUser, school.getId() + "News");
            }
            pushService.addTopic(settingUser, "news");
            settingUserRepository.saveAndFlush(settingUser);
            if(!ObjectUtils.isEmpty(body.secFr)) {
                settingUser.setSecFr(body.secFr);
            }
        }
        return null;
    }

    @Override
    public AuthOutDTO setupInviteCode(User invitedUser, Long schId) {
        final var dtoBuilder = AuthOutDTO.builder();
        final UUID uuid = UUID.randomUUID();
        final Instant after = Instant.now().plus(Duration.ofDays(30));
        final Date dateAfter = Date.from(after);

        invitedUser.setCode(uuid.toString());
        invitedUser.setExpDate(AppConfig.dataFormat.format(dateAfter));
        userRepository.saveAndFlush(invitedUser);

        log.debug("setCode " + uuid);

        dtoBuilder.code(uuid.toString())
            .id(invitedUser.getId())
            .id1(schId);
        return dtoBuilder.build();
    }
}
