package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class School {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "hteachers")
    private List<Long> hteachers;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "hteachersInv")
    private List<Long> hteachersInv;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "news")
    private List<Long> news;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "groups")
    private List<Long> groups;

    @Column(name = "contacts")
    private Long contacts;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "teachers")
    private List<Long> teachers;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "teachersInv")
    private List<Long> teachersInv;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "subjects")
    private List<Long> subjects;

    public School(String name) {
        this.name = name;
    }

    public School(String name, List<Long> hteachers) {
        this.name = name;
        this.hteachers = hteachers;
    }

    public School(List<Long> hteachers, String name, List<Long> hteachersInv) {
        this.name = name;
        this.hteachers = hteachers;
        this.hteachersInv = hteachersInv;
    }

    public School(String name, List<Long> hteachers, List<Long> news, Long contacts, List<Long> groups, List<Long> subjects, List<Long> teachers) {
        this.name = name;
        this.hteachers = hteachers;
        this.news = news;
        this.contacts = contacts;
        this.groups = groups;
        this.subjects = subjects;
        this.teachers = teachers;
    }

    public School(String name, List<Long> hteachers, List<Long> hteachersInv, List<Long> news, Long contacts, List<Long> groups, List<Long> subjects, List<Long> teachers) {
        this.name = name;
        this.hteachers = hteachers;
        this.hteachersInv = hteachersInv;
        this.news = news;
        this.contacts = contacts;
        this.groups = groups;
        this.subjects = subjects;
        this.teachers = teachers;
    }

    public List<Long> getHteachers() {
        if(hteachers == null) {
            hteachers = new ArrayList<>(asList());
        }
        return hteachers;
    }

    public List<Long> getHteachersInv() {
        if(hteachersInv == null) {
            hteachersInv = new ArrayList<>(asList());
        }
        return hteachersInv;
    }

    public List<Long> getNews() {
        if(news == null) {
            news = new ArrayList<>(asList());
        }
        return news;
    }

    public List<Long> getGroups() {
        if(groups == null) {
            groups = new ArrayList<>(asList());
        }
        return groups;
    }

    public List<Long> getTeachers() {
        if(teachers == null) {
            teachers = new ArrayList<>(asList());
        }
        return teachers;
    }

    public List<Long> getTeachersInv() {
        if(teachersInv == null) {
            teachersInv = new ArrayList<>(asList());
        }
        return teachersInv;
    }

    public List<Long> getSubjects() {
        if(subjects == null) {
            subjects = new ArrayList<>(asList());
        }
        return subjects;
    }
}
