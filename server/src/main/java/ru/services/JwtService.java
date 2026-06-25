package ru.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.data.DAO.auth.User;
import ru.services.interfaces.IJwtService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService implements IJwtService {
    private static final String BEGIN_SECRET = System.getProperty("beginSecret");
    private static final String MIDDLE_SECRET = System.getProperty("middleSecret");
    private static final String END_SECRET = System.getProperty("endSecret");

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationMinutesInMs;

    @Override
    public String generateToken(User user) {
        final int MAX_LENGTH_EXP = 10;
        final Date now = new Date(System.currentTimeMillis());
        final Date expiryDate = new Date(now.getTime() + jwtExpirationMinutesInMs);
        final String uid = user.getId() + "";
        final String uidInSecret = uidSecret(uid);
        final String expInSecret = (expiryDate.getTime() + "").substring(0, MAX_LENGTH_EXP);
        final String secret = BEGIN_SECRET + expInSecret + uidInSecret + MIDDLE_SECRET + expInSecret + uidInSecret + END_SECRET;

        return Jwts.builder()
            .issuer("ejournal")
            .subject(uid)
            .claim("role", user.getRoles().keySet())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getKeySpec(secret))
            .compact();
    }

    private String uidSecret(String uid) {
        final int MAX_LENGTH_LONG = 13;
        if(uid.length() < MAX_LENGTH_LONG) {
            final String emptyString = "0";
            return emptyString.repeat(MAX_LENGTH_LONG - uid.length()) + uid;
        }
        return uid;
    }

    private SecretKey getKeySpec(String secret) {
        if(secret == null) return null;

        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Claims getInfoFromJWT(String token) {
        log.trace("token " + token);
        final String secret = secretFromTokenInBase64(token);
        return Jwts.parser()
            .verifyWith(getKeySpec(secret))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private String secretFromTokenInBase64(String authToken) {
        final int NUMBER_OF_JWT_PARTS_IN_BASE64 = 3;

        if(ObjectUtils.isEmpty(authToken)) return null;

        final String[] splittedToken = authToken.split("\\.");
        if(splittedToken.length < NUMBER_OF_JWT_PARTS_IN_BASE64) return null;

        final String bodyTokenInBase64 = splittedToken[1];
        final String jsonBodyToken = new String(Base64.getDecoder().decode(bodyTokenInBase64), StandardCharsets.UTF_8);
        final JsonObject jsonObject = JsonParser.parseString(jsonBodyToken).getAsJsonObject();
        final String expiryDate = jsonObject.get("exp").getAsString();
        final String uidInSecret = uidSecret(jsonObject.get("sub").getAsString());
        return BEGIN_SECRET + expiryDate + uidInSecret + MIDDLE_SECRET + expiryDate + uidInSecret + END_SECRET;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getKeySpec("a-striiiiiing-secret-at-least-384-bits-looooooong"))
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException |
             IllegalArgumentException ignored) {
        }
        return false;
    }
}