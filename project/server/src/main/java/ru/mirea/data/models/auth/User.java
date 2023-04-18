package ru.mirea.data.models.auth;

import lombok.*;
import ru.mirea.data.converters.MapRoleConverter;
import ru.mirea.data.json.Role;

import javax.persistence.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "useer") public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "pass")
    private String password;

    @Column(name = "code")
    private String code;

    @Column(name = "date")
    private String expDate;

    @Column(name = "fio")
    private String fio;

    @Column(name = "secFr")
    private String secFr;

    @Column(name = "ico")
    private int ico;

    @Convert(converter = MapRoleConverter.class)
    @Column(name = "roles")
    private Map<Long, Role> roles;

    @Column(name = "info")
    private String info;

    public User(String login, String password, int ico) {
        this.login = login;
        this.password = password;
        this.ico = ico;
    }

    public User(String login, String password, String fio, int ico, Map<Long,Role> roles) {
        this.login = login;
        this.fio = fio;
        this.password = password;
        this.ico = ico;
        this.roles = roles;
    }
}