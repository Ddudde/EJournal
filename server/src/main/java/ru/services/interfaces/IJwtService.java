package ru.services.interfaces;

import io.jsonwebtoken.Claims;
import ru.data.DAO.auth.User;

public interface IJwtService {
    String generateToken(User user);

    Claims getInfoFromJWT(String token);
}
