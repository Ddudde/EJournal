package ru.mirea.data.models.school.dayOfWeek;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Lesson {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "subject")
    private Long subject;

    @Column(name = "teacher")
    private Long teacher;

    @Column(name = "teacherInv")
    private Long teacherInv;

    @Column(name = "kab")
    private String kab;

    public Lesson(Long subject, Long teacher, String kab) {
        this.subject = subject;
        this.teacher = teacher;
        this.kab = kab;
    }
}