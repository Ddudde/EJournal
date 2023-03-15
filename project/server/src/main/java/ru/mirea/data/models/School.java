package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.List;

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

    public School(String name, List<Long> hteachers, List<Long> news, Long contacts, List<Long> groups) {
        this.name = name;
        this.hteachers = hteachers;
        this.news = news;
        this.contacts = contacts;
        this.groups = groups;
    }

    public School(String name, List<Long> hteachers, List<Long> hteachersInv, List<Long> news, Long contacts, List<Long> groups) {
        this.name = name;
        this.hteachers = hteachers;
        this.hteachersInv = hteachersInv;
        this.news = news;
        this.contacts = contacts;
        this.groups = groups;
    }
}
