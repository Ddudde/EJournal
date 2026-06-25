package ru.services.interfaces.logic;

import ru.data.DAO.auth.User;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;
import ru.data.DTO.controller.auth.AuthServiceDTO;
import ru.security.user.AuthToken;

public interface IAuthService {
    AuthOutDTO prepareConnection(AuthInnerDTO body, User user);

    AuthServiceDTO authUser(AuthInnerDTO body, AuthToken auth, User user);

    AuthOutDTO createUser(User invitedUser, AuthInnerDTO body);

    AuthOutDTO setupInviteCode(User invitedUser, Long schId);

    AuthServiceDTO validateRefresh(String token);
}
