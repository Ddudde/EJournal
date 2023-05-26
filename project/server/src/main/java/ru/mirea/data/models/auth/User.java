package ru.mirea.data.models.auth;

import lombok.*;
import ru.mirea.data.MapRoleConverter;
import ru.mirea.data.json.Role;

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

    private Long selRole, selKid, settings;

    @Convert(converter = MapRoleConverter.class)
    @Column(columnDefinition="CLOB")
    private Map<Long, Role> roles;

    public User(String login, String password, Long settings) {
        this.login = login;
        this.password = password;
        this.settings = settings;
    }

    public User(String login, String password, String fio, Map<Long, Role> roles, Long selRole, Long settings) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.roles = roles;
        this.settings = settings;
    }

    public User(String login, String password, String fio, Map<Long, Role> roles, Long selRole, Long selKid, Long settings) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.selKid = selKid;
        this.roles = roles;
        this.settings = settings;
    }

    public Map<Long, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }
}