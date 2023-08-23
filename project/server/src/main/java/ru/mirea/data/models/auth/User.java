package ru.mirea.data.models.auth;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "useer") public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String login, password, code, expDate, fio;

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

    public User(String fio, Map<Long, Role> roles, String expDate, String code) {//inv
        this.fio = fio;
        this.code = code;
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

    public User(String login, String password, SettingUser settings) {
        this.login = login;
        this.password = password;
        this.settings = settings;
    }

    public User(String login, String password, String fio, Map<Long, Role> roles, Long selRole, SettingUser settings) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.roles = new HashMap<>(roles);
        this.settings = settings;
    }

    public User(String login, String password, String fio, Map<Long, Role> roles, Long selRole, Long selKid, SettingUser settings) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.selKid = selKid;
        this.roles = new HashMap<>(roles);
        this.settings = settings;
    }

//    @PreRemove
    public void rem() {
        getRoles().clear();
    }

    public Map<Long, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }

    public Role getRole(Long role) {
        return getRoles().get(role);
    }

    public SettingUser getSettings() {
        if(settings == null) settings = new SettingUser();
        return settings;
    }
}