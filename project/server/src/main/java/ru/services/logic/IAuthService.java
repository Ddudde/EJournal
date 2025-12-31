package ru.services.logic;

import ru.data.DAO.auth.User;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.auth.AuthInnerDTO;
import ru.data.DTO.controller.auth.AuthOutDTO;

public interface IAuthService {
    AuthOutDTO prepareConnection(AuthInnerDTO body, SubscriberDTO sub, User user);

    AuthOutDTO authUser(AuthInnerDTO body, String uuid, User user);

    AuthOutDTO createUser(User invitedUser, AuthInnerDTO body);

    AuthOutDTO setupInviteCode(User invitedUser, Long schId);
}
