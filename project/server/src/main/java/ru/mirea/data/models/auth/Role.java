package ru.mirea.data.models.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;

import javax.persistence.*;
import java.util.ArrayList;
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
    @OneToMany
    @JoinColumn(name = "role_kid_id")
    private List<Invite> kidsInv;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "role_par_id")
    private List<Invite> parentsInv;

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
        this.subjects = subjects;
    }

    public Role(String email, School YO) { //zav, par
        this.email = email;
        this.YO = YO;
    }

    public Role(String email) { //adm
        this.email = email;
    }

    public Set<String> getSubjects() {
        if(subjects == null) subjects = Set.of();
        return subjects;
    }

    public List<User> getKids() {
        if(kids == null) kids = new ArrayList<>();
        return kids;
    }

    public List<Invite> getKidsInv() {
        if(kidsInv == null) kidsInv = new ArrayList<>();
        return kidsInv;
    }

    public List<User> getParents() {
        if(parents == null) parents = new ArrayList<>();
        return parents;
    }

    public List<Invite> getParentsInv() {
        if(parentsInv == null) parentsInv = new ArrayList<>();
        return parentsInv;
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

    public String getToStringI(List<Invite> invites) {
        String rez = null;
        if(invites == null) return "[]";
        for(Invite invite : invites) {
            if(rez == null) {
                rez = "[";
            } else {
                rez += ", ";
            }
            rez += "Invite{id=" + invite.getId() + "}";
        }
        return rez + "]";
    }

    @Override
    public String toString() {
        String gr = grp == null ? "null" : grp.getId()+"";
        return "Role{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", YO=" + YO.getId() +
                ", grp=" + gr +
                ", kids=" + getToStringU(kids) +
                ", parents=" + getToStringU(parents) +
                ", kidsInv=" + getToStringI(kidsInv) +
                ", parentsInv=" + getToStringI(parentsInv) +
                ", subjects=" + subjects +
                '}';
    }
}
