package ru.data.models.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.data.models.school.Group;
import ru.data.models.school.School;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "rol") public class Role {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String email;

    @OneToOne
    private School YO;

    @OneToOne
    private Group grp;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "role_kid_id")
    private List<User> kids;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "role_par_id")
    private List<User> parents;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ElementCollection
    private Set<String> subjects;

    public Role(String email, School YO, Group grp) { // kid
        this.email = email;
        this.YO = YO;
        this.grp = grp;
    }

    public Role(String email, School YO, List<User> kids) { // par
        this.email = email;
        this.YO = YO;
        this.kids = new ArrayList<>(kids);
    }

    public Role(String email, Set<String> subjects, School YO) { // tea
        this.email = email;
        this.YO = YO;
        this.subjects = new HashSet<>(subjects);
    }

    public Role(String email, School YO) { //zav, par
        this.email = email;
        this.YO = YO;
    }

    public Role(String email) { //adm
        this.email = email;
    }

    public Set<String> getSubjects() {
        if(subjects == null) subjects = new HashSet<>();
        return subjects;
    }

    public List<User> getKids() {
        if(kids == null) kids = new ArrayList<>();
        return kids;
    }

    public List<User> getParents() {
        if(parents == null) parents = new ArrayList<>();
        return parents;
    }

    public String getToStringU(List<User> users) {
        String rez = null;
        if(users == null) return "[]";
        for(User user : users) {
            if(rez == null) {
                rez = "[";
            } else {
                rez += ", ";
            }
            rez += "User{id=" + user.getId() + "}";
        }
        return rez + "]";
    }

    @Override
    public String toString() {
        String yo = YO == null ? "null" : YO.getId()+"";
        String gr = grp == null ? "null" : grp.getId()+"";
        return "Role{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", YO=" + yo +
                ", grp=" + gr +
                ", kids=" + getToStringU(kids) +
                ", parents=" + getToStringU(parents) +
                ", subjects=" + subjects +
                '}';
    }
}
