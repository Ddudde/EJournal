package ru.data.DAO.school;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.data.DAO.auth.User;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "grp") public class Group {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "grp_kid_id")
    private List<User> kids;

    public Group(String name) {
        this.name = name;
    }

//    @PreRemove
//    private void rem() {
//        System.out.println(name);
//        getKids().clear();
//    }

    public List<User> getKids() {
        if(kids == null) kids = new ArrayList<>();
        return kids;
    }
}