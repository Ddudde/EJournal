package ru.data.DAO.school;

import jakarta.persistence.*;
import lombok.*;
import ru.data.DAO.auth.User;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Lesson {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private User teacher;

    @ManyToOne
    private School school;

    @ManyToOne
    private Group grp;

    private int dayWeek, numLesson;

    private String kab, nameSubject;

    public Lesson(School school, Group grp, int dayWeek, int numLesson, String kab, String nameSubject, User teacher) {
        this.school = school;
        this.grp = grp;
        this.dayWeek = dayWeek;
        this.numLesson = numLesson;
        this.kab = kab;
        this.nameSubject = nameSubject;
        this.teacher = teacher;
    }
}