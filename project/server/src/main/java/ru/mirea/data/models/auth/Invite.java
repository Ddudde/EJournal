package ru.mirea.data.models.auth;

import lombok.*;
import ru.mirea.data.converters.MapRoleConverter;
import ru.mirea.data.json.Role;

import javax.persistence.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Invite {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "fio")
    private String fio;

    @Column(name = "code")
    private String code;

    @Column(name = "date")
    private String expDate;

    @Convert(converter = MapRoleConverter.class)
    @Column(name = "role")
    private Map<Long, Role> role;

    public Invite(String fio, Map<Long, Role> role, String expDate) {
        this.fio = fio;
        this.expDate = expDate;
        this.role = role;
    }
}