package ru.mirea.data.models.school.dayOfWeek;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Subject {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "school")
    private Long school;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "teachers")
    private List<Long> teachers;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "teachersInv")
    private List<Long> teachersInv;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "days")
    private List<Long> days;

    public List<Long> getTeachersInv() {
        if(teachersInv == null) {
            teachersInv = new ArrayList<>(asList());
        }
        return teachersInv;
    }

    public List<Long> getTeachers() {
        if(teachers == null) {
            teachers = new ArrayList<>(asList());
        }
        return teachers;
    }

    public Subject(String name, List<Long> teachers) {
        this.name = name;
        this.teachers = teachers;
    }

    public Subject(String name, Long school, List<Long> teachers) {
        this.name = name;
        this.school = school;
        this.teachers = teachers;
    }
}