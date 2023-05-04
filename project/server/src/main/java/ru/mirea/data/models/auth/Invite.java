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
@Entity public class Invite {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String fio, code, expDate;

    @Convert(converter = MapRoleConverter.class)
    @Column(columnDefinition="CLOB")
    private Map<Long, Role> role;

    public Invite(String fio, Map<Long, Role> role, String expDate) {
        this.fio = fio;
        this.expDate = expDate;
        this.role = role;
    }

    public Map<Long, Role> getRole() {
        if(role == null) role = new HashMap<>();
        return role;
    }
}