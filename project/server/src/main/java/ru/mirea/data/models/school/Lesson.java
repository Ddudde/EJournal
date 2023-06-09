package ru.mirea.data.models.school;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Lesson {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private Long teacher, teacherInv, grp, school;

    private int dayWeek, numLesson;

    private String kab, nameSubject;

    public Lesson(Long school, Long teacher, Long grp, int dayWeek, int numLesson, String kab, String nameSubject) {
        this.school = school;
        this.teacher = teacher;
        this.grp = grp;
        this.dayWeek = dayWeek;
        this.numLesson = numLesson;
        this.kab = kab;
        this.nameSubject = nameSubject;
    }
}