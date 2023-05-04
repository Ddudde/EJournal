package ru.mirea.data.models.school.dayOfWeek;

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

    private Long subject, teacher, teacherInv;

    private String kab;

    public Lesson(Long subject, Long teacher, String kab) {
        this.subject = subject;
        this.teacher = teacher;
        this.kab = kab;
    }
}