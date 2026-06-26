package ru.services.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.configs.SecurityConfig;
import ru.controllers.AuthController;
import ru.data.DAO.auth.RefreshToken;
import ru.data.DAO.auth.Role;
import ru.data.DAO.auth.SettingUser;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;
import ru.data.DTO.controller.auth.AuthServiceDTO;
import ru.data.reps.auth.RefreshTokenRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.security.user.AuthToken;
import ru.security.user.Roles;
import ru.services.interfaces.IJwtService;
import ru.services.interfaces.IPushService;
import ru.services.interfaces.db.IDBService;
import ru.services.interfaces.logic.IAuthService;

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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IDBService dbService;
    private final UserRepository userRepository;
    private final IJwtService jwtService;

    /** RU: 30 дней */
    private final long TIME_OF_LIVE_REFRESH_TOKENS = 86400000 * 30L;

    /** RU: 30 дней */
    public final int TIME_OF_LIFE_IN_SECONDS_REFRESH_TOKEN = 60 * 60 * 24 * 30;

    @Override
    public AuthOutDTO prepareConnection(AuthInnerDTO body, User user) {
        final var dtoBuilder = AuthOutDTO.builder();

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
            dtoBuilder.kid(serviceDTO.kid)
                .kids(serviceDTO.kids);
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
    public AuthServiceDTO authUser(AuthInnerDTO body, AuthToken auth, User user) {
        final var dtoBuilder = AuthServiceDTO.builder();
        final var bodyDtoBuilder = AuthOutDTO.builder();
        final SettingUser settingUser = user.getSettings();
        final Map<Roles, Role> roles = user.getRoles();
        final ResponseCookie cookie = prepareNewRefreshToken(user);

        if(!ObjectUtils.isEmpty(body.notifToken)) {
            if(body.permis) {
                pushService.addToken(settingUser, body.notifToken);
            } else {
                pushService.remToken(settingUser, body.notifToken);
            }
            settingUserRepository.saveAndFlush(settingUser);
        }
        final AuthServiceDTO serviceDTO = prepareParentInfo(user);
        bodyDtoBuilder.auth(true)
            .login(user.getUsername())
            .role(user.getSelRole().i)
            .uuidS(auth.getUUID())
            .ico(settingUser.getIco())
            .roles(!ObjectUtils.isEmpty(roles) && roles.size() > 1)
            .secFr(!ObjectUtils.isEmpty(settingUser.getSecFr()))
            .email(!ObjectUtils.isEmpty(settingUser.getEmail()));

        if(serviceDTO != null) {
            bodyDtoBuilder.kid(serviceDTO.kid)
                .kids(serviceDTO.kids);
        }
        dtoBuilder.bodyAuth(bodyDtoBuilder.build())
            .token(auth.getJWTToken())
            .cookie(cookie.toString());
        return dtoBuilder.build();
    }

    private ResponseCookie prepareNewRefreshToken(User user) {
        final RefreshToken refreshToken = new RefreshToken();
        final UUID uuidRefreshToken = UUID.randomUUID();
        final long now = System.currentTimeMillis();

        refreshToken.setValue(uuidRefreshToken.toString());
        refreshToken.setUsr(user);
        refreshToken.setTimeOfExpired(now + TIME_OF_LIVE_REFRESH_TOKENS);
        refreshTokenRepository.saveAndFlush(refreshToken);

        return ResponseCookie.from(SecurityConfig.NAME_OF_COOKIE, refreshToken.getValue()).path("/auth/refreshToken").httpOnly(true)
            .maxAge(TIME_OF_LIFE_IN_SECONDS_REFRESH_TOKEN).sameSite("Strict").build();
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

    @Override
    public AuthServiceDTO validateRefresh(String token) {
        final var dtoBuilder = AuthServiceDTO.builder();
        if (token == null) return null;

        final RefreshToken refreshToken = refreshTokenRepository.findByValue(token);
        if(refreshToken == null) return null;

        final ResponseCookie cookie = prepareNewRefreshToken(refreshToken.getUsr());
        final String JWTToken = jwtService.generateToken(refreshToken.getUsr());
        refreshTokenRepository.delete(refreshToken);
        dtoBuilder.token(JWTToken)
            .cookie(cookie.toString());
        return dtoBuilder.build();
    }
}
