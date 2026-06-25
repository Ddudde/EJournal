package ru.security.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import ru.data.DTO.SubscriberDTO;
import ru.security.CustomProvider;

import java.util.Collection;
import java.util.Set;

@Getter @Setter @ToString
public class AuthToken extends UsernamePasswordAuthenticationToken {
    private SubscriberDTO sub;
    private String UUID;
    private Long userId;
    private String JWTToken;

    public AuthToken(Long userId, String UUID) {
        super("", "", CustomProvider.getAuthorities(Set.of("ANONYMOUS")));
        this.userId = userId;
        this.UUID = UUID;
    }

    public AuthToken(Collection<? extends GrantedAuthority> authorities, Long userId, String JWTToken) {
        super("", "", authorities);
        this.userId = userId;
        this.JWTToken = JWTToken;
    }

    public AuthToken(Collection<? extends GrantedAuthority> authorities, SubscriberDTO sub, String UUID, Long userId, String JWTToken) {
        super(sub, "", authorities);
        this.sub = sub;
        this.UUID = UUID;
        this.userId = userId;
        this.JWTToken = JWTToken;
    }

    public AuthToken(Collection<? extends GrantedAuthority> authorities, SubscriberDTO sub, String UUID, Long userId) {
        super(sub, "", authorities);
        this.sub = sub;
        this.UUID = UUID;
        this.userId = userId;
    }

    public AuthToken() {
        super("anonymousUser", "", CustomProvider.getAuthorities(Set.of("ANONYMOUS")));
    }

    public AuthToken(SubscriberDTO sub, String UUID) {
        super(sub, "", CustomProvider.getAuthorities(Set.of("ANONYMOUS")));
        this.sub = sub;
        this.UUID = UUID;
    }
}
