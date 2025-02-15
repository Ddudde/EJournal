package ru.security.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import ru.data.DTO.SubscriberDTO;
import ru.security.CustomProvider;

import java.util.Collection;
import java.util.Set;

@Getter @Setter
public class CustomToken extends UsernamePasswordAuthenticationToken {
    private SubscriberDTO sub;
    private String UUID;

    public CustomToken(Object credentials, Collection<? extends GrantedAuthority> authorities, SubscriberDTO sub, String UUID) {
        super(sub, credentials, authorities);
        this.sub = sub;
        this.UUID = UUID;
    }

    public CustomToken() {
        super("anonymousUser", "", CustomProvider.getAuthorities(Set.of("ANONYMOUS")));
    }

    public CustomToken(SubscriberDTO sub, String UUID) {
        super(sub, "", CustomProvider.getAuthorities(Set.of("ANONYMOUS")));
        this.sub = sub;
        this.UUID = UUID;
    }
}
