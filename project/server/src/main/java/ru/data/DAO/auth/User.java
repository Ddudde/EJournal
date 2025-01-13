package ru.data.DAO.auth;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.security.user.Roles;

import javax.annotation.PreDestroy;
import javax.persistence.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "useer") public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String username, password, code, expDate, fio;

    private Roles selRole;

    private Long selKid;

    @OneToOne(orphanRemoval = true)
    private SettingUser settings;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @MapKeyColumn(name = "role")
    private Map<Roles, Role> roles;

    public User(String fio, Map<Roles, Role> roles, String expDate) {//inv
        this.fio = fio;
        this.expDate = expDate;
        this.roles = new HashMap<>(roles);
    }

    public User(String fio, Map<Roles, Role> roles, Roles selRole, String expDate, String code) {//inv
        this.fio = fio;
        this.code = code;
        this.expDate = expDate;
        this.selRole = selRole;
        this.roles = new HashMap<>(roles);
    }

    public User(String username, String password, String fio, Map<Roles, Role> roles, Roles selRole, SettingUser settings) {
        this.username = username;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.roles = new HashMap<>(roles);
        this.settings = settings;
    }

//    @PreRemove
    @PreDestroy
    public void rem() {
        getRoles().clear();
        settings = null;
    }

    public Map<Roles, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }

    public Role getRole(Roles role) {
        return getRoles().get(role);
    }

    public Role getSelecRole() {
        return getRole(selRole);
    }

    public SettingUser getSettings() {
        if(settings == null) settings = new SettingUser();
        return settings;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.keySet().stream().map(String::valueOf)
            .map(SimpleGrantedAuthority::new)
            .toList();
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