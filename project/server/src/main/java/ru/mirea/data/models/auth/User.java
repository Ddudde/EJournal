package ru.mirea.data.models.auth;

import lombok.*;
import ru.mirea.data.MapRoleConverter;
import ru.mirea.data.SetStringConverter;
import ru.mirea.data.json.Role;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "useer") public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String login, password, code, expDate, fio, secFr, info;

    private Long selRole, selKid;

    private int ico;

    @Convert(converter = MapRoleConverter.class)
    @Column(columnDefinition="CLOB")
    private Map<Long, Role> roles;

    @Convert(converter = SetStringConverter.class)
    @Column(columnDefinition="CLOB")
    private Set<String> tokens, topics;

    public User(String login, String password, int ico) {
        this.login = login;
        this.password = password;
        this.ico = ico;
    }

    public User(String login, String password, String fio, int ico, Map<Long, Role> roles, Long selRole) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.ico = ico;
        this.roles = roles;
    }

    public User(String login, String password, String fio, int ico, Map<Long, Role> roles, Long selRole, Long selKid) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.selKid = selKid;
        this.ico = ico;
        this.roles = roles;
    }

    public User(String login, String password, String fio, int ico, Map<Long, Role> roles, Long selRole, Long selKid, Set<String> topics) {
        this.login = login;
        this.password = password;
        this.fio = fio;
        this.selRole = selRole;
        this.selKid = selKid;
        this.ico = ico;
        this.roles = roles;
        this.topics = topics;
    }

    public Map<Long, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }

    public Set<String> getTokens() {
        if(tokens == null) tokens = new HashSet<>();
        return tokens;
    }

    public Set<String> getTopics() {
        if(topics == null) topics = new HashSet<>();
        return topics;
    }
}