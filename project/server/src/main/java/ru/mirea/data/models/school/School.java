package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.ListLongConverter;

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

    @Convert(converter = ListLongConverter.class)
    @Column(columnDefinition="CLOB")
    private List<Long> hteachers, hteachersInv, news, groups,
            teachers, teachersInv, periods;

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

    public School(String name, List<Long> hteachers, List<Long> news, Long contacts, List<Long> groups, List<Long> teachers, List<Long> periods) {
        this.name = name;
        this.hteachers = new ArrayList<>(hteachers);
        this.news = new ArrayList<>(news);
        this.contacts = contacts;
        this.groups = new ArrayList<>(groups);
        this.teachers = new ArrayList<>(teachers);
        this.periods = new ArrayList<>(periods);
    }

    public School(String name, List<Long> hteachers, List<Long> hteachersInv, List<Long> news, Long contacts, List<Long> groups, List<Long> teachers, List<Long> periods) {
        this.name = name;
        this.hteachers = new ArrayList<>(hteachers);
        this.hteachersInv = new ArrayList<>(hteachersInv);
        this.news = new ArrayList<>(news);
        this.contacts = contacts;
        this.groups = new ArrayList<>(groups);
        this.teachers = new ArrayList<>(teachers);
        this.periods = new ArrayList<>(periods);
    }

    public List<Long> getHteachers() {
        if(hteachers == null) hteachers = new ArrayList<>();
        return hteachers;
    }

    public List<Long> getHteachersInv() {
        if(hteachersInv == null) hteachersInv = new ArrayList<>();
        return hteachersInv;
    }

    public List<Long> getNews() {
        if(news == null) news = new ArrayList<>();
        return news;
    }

    public List<Long> getGroups() {
        if(groups == null) groups = new ArrayList<>();
        return groups;
    }

    public List<Long> getTeachers() {
        if(teachers == null) teachers = new ArrayList<>();
        return teachers;
    }

    public List<Long> getTeachersInv() {
        if(teachersInv == null) teachersInv = new ArrayList<>();
        return teachersInv;
    }

    public List<Long> getPeriods() {
        if(periods == null) periods = new ArrayList<>();
        return periods;
    }
}
