package ru.data.models.school;

import lombok.*;
import ru.data.models.auth.User;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Lesson {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User teacher;

    @OneToOne
    private School school;

    @OneToOne
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