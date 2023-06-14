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
@Entity public class Invite {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String fio, code, expDate;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "invite_id")
    @MapKeyColumn(name = "role")
    private Map<Long, Role> roles;

    public Invite(String fio, Map<Long, Role> roles, String expDate) {
        this.fio = fio;
        this.expDate = expDate;
        this.roles = roles;
    }

    public Invite(String fio, Map<Long, Role> roles, String expDate, String code) {
        this.fio = fio;
        this.code = code;
        this.expDate = expDate;
        this.roles = roles;
    }

    public Map<Long, Role> getRoles() {
        if(roles == null) roles = new HashMap<>();
        return roles;
    }
}