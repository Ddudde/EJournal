package ru.mirea.data.models.school;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.mirea.data.models.Contacts;
import ru.mirea.data.models.News;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class School {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "sch_htea_id")
    private List<User> hteachers;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "sch_htea_id")
    private List<Invite> hteachersInv;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "sch_tea_id")
    private List<User> teachers;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "sch_tea_id")
    private List<Invite> teachersInv;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "sch_id")
    private List<Group> groups;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "sch_id")
    private List<News> news;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "sch_id")
    private List<Period> periods;

    @OneToOne(orphanRemoval = true)
    private Contacts contacts;

    public School(String name) {
        this.name = name;
    }

    public School(String name, List<User> hteachers) {
        this.name = name;
        this.hteachers = hteachers;
    }

    public School(List<User> hteachers, String name, List<Invite> hteachersInv) {
        this.name = name;
        this.hteachers = hteachers;
        this.hteachersInv = hteachersInv;
    }

    public School(String name, List<News> news, Contacts contacts, List<Group> groups, List<Period> periods) {
        this.name = name;
        this.news = new ArrayList<>(news);
        this.contacts = contacts;
        this.groups = new ArrayList<>(groups);
        this.periods = new ArrayList<>(periods);
    }

    public List<User> getHteachers() {
        if(hteachers == null) hteachers = new ArrayList<>();
        return hteachers;
    }

    public List<Invite> getHteachersInv() {
        if(hteachersInv == null) hteachersInv = new ArrayList<>();
        return hteachersInv;
    }

    public List<News> getNews() {
        if(news == null) news = new ArrayList<>();
        return news;
    }

    public List<Group> getGroups() {
        if(groups == null) groups = new ArrayList<>();
        return groups;
    }

    public List<User> getTeachers() {
        if(teachers == null) teachers = new ArrayList<>();
        return teachers;
    }

    public List<Invite> getTeachersInv() {
        if(teachersInv == null) teachersInv = new ArrayList<>();
        return teachersInv;
    }

    public List<Period> getPeriods() {
        if(periods == null) periods = new ArrayList<>();
        return periods;
    }
}
