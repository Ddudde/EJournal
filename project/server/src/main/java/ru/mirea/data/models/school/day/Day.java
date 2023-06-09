package ru.mirea.data.models.school.day;

import lombok.*;
import ru.mirea.data.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "daay") public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long teacher, teacherInv, grp, school;

    @Column(columnDefinition="CLOB")
    private String homework;

    private String nameSubject, dat;

    @Column(columnDefinition="CLOB")
    @Convert(converter = ListLongConverter.class)
    private List<Long> marks;

    public Day(Long school, Long teacher, Long grp, String nameSubject, String homework, String dat, List<Long> marks) {
        this.teacher = teacher;
        this.grp = grp;
        this.school = school;
        this.homework = homework;
        this.nameSubject = nameSubject;
        this.dat = dat;
        this.marks = marks;
    }

    public List<Long> getMarks() {
        if(marks == null) marks = new ArrayList<>();
        return marks;
    }
}