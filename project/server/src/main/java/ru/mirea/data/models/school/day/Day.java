package ru.mirea.data.models.school.day;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "daay") public class Day {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "date")
    private String date;

    @Column(name = "teacher")
    private Long teacher;

    @Column(name = "teacherInv")
    private Long teacherInv;

    @Column(name = "grp")
    private Long group;

    @Column(name = "homework")
    private String homework;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "marks")
    private List<Long> marks;

    public Day(String date) {
        this.date = date;
    }

    public List<Long> getMarks() {
        if(marks == null) {
            marks = new ArrayList<>(asList());
        }
        return marks;
    }
}