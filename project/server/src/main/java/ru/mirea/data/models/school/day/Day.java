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
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String date;

    private Long teacher, teacherInv;

    @Column(name = "grp")
    private Long group;

    @Column(columnDefinition="CLOB")
    private String homework;

    @Convert(converter = ListLongConverter.class)
    private List<Long> marks;

    public Day(String date) {
        this.date = date;
    }

    public List<Long> getMarks() {
        if(marks == null) marks = new ArrayList<>();
        return marks;
    }
}