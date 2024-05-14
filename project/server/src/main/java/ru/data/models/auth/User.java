package ru.data.models.auth;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "useer") public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String username, password, code, expDate, fio;

    private Long selRole, selKid;

    @OneToOne(orphanRemoval = true)
    private SettingUser settings;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @MapKeyColumn(name = "role")
    private Map<Long, Role> roles;

    public User(String fio, Map<Long, Role> roles, String expDate) {//inv
        this.fio = fio;
        this.expDate = expDate;
        this.roles = new HashMap<>(roles);
    }

    public User(String fio, Map<Long, Role> roles, Long selRole, String expDate, String code) {//inv
        this.fio = fio;
        this.code = code;
        this.expDate = expDate;
        this.selRole = selRole;
        this.roles = new HashMap<>(roles);
    }

    public User(String username, String password, String fio, Map<Long, Role> roles, Long selRole, SettingUser settings) {
        this.username = username;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.roles = new HashMap<>(roles);
        this.settings = settings;
    }

//    @PreRemove
    public void rem() {
        getRoles().clear();
        settings = null;
    }

    public Map<Long, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }

    public Role getRole(Long role) {
        return getRoles().get(role);
    }

    public Role getSelecRole() {
        return getRoles().get(selRole);
    }

    public SettingUser getSettings() {
        if(settings == null) settings = new SettingUser();
        return settings;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.keySet().stream().map(String::valueOf)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}