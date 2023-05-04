package ru.mirea.data.models.school.dayOfWeek;

import lombok.*;
import ru.mirea.data.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Subject {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    private Long school;

    @Convert(converter = ListLongConverter.class)
    @Column(columnDefinition="CLOB")
    private List<Long> teachers, teachersInv, days;

    public Subject(String name, List<Long> teachers) {
        this.name = name;
        this.teachers = teachers;
    }

    public Subject(String name, Long school, List<Long> teachers) {
        this.name = name;
        this.school = school;
        this.teachers = teachers;
    }

    public List<Long> getTeachersInv() {
        if(teachersInv == null) teachersInv = new ArrayList<>();
        return teachersInv;
    }

    public List<Long> getTeachers() {
        if(teachers == null) teachers = new ArrayList<>();
        return teachers;
    }
}