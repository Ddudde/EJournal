package ru.mirea.data.models.school;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "grp") public class Group {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "grp_kid_id")
    private List<User> kids;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "grp_kid_id")
    private List<Invite> kidsInv;

    public Group(String name) {
        this.name = name;
    }

    public List<User> getKids() {
        if(kids == null) kids = new ArrayList<>();
        return kids;
    }

    public List<Invite> getKidsInv() {
        if(kidsInv == null) kidsInv = new ArrayList<>();
        return kidsInv;
    }
}