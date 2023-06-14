package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;

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
    private Invite teacherInv;

    @OneToOne
    private School school;

    @OneToOne
    private Group grp;

    private int dayWeek, numLesson;

    private String kab, nameSubject;

    public Lesson(School school, Group grp, int dayWeek, int numLesson, String kab, String nameSubject) {
        this.school = school;
        this.grp = grp;
        this.dayWeek = dayWeek;
        this.numLesson = numLesson;
        this.kab = kab;
        this.nameSubject = nameSubject;
    }
}