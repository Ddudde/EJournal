package ru.mirea.data.models.school;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.mirea.data.models.auth.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "daay") public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User teacher;

    @OneToOne
    private School school;

    @OneToOne
    private Group grp;

    @Column(columnDefinition="text")
    private String homework;

    private String nameSubject, dat;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "day_id")
    private List<Mark> marks;

    public Day(School school, User teacher, Group grp, String nameSubject, String homework, String dat, List<Mark> marks) {
        this.teacher = teacher;
        this.grp = grp;
        this.school = school;
        this.homework = homework;
        this.nameSubject = nameSubject;
        this.dat = dat;
        this.marks = new ArrayList<>(marks);
    }

    public List<Mark> getMarks() {
        if(marks == null) marks = new ArrayList<>();
        return marks;
    }

    @Override
    public String toString() {
        String gr = grp == null ? "null" : grp.getId()+"";
        return "Day{" +
                "id=" + id +
                ", teacher=" + teacher +
                ", school=" + school.getId() +
                ", grp=" + gr +
                ", homework='" + homework + '\'' +
                ", nameSubject='" + nameSubject + '\'' +
                ", dat='" + dat + '\'' +
                ", marks=" + marks +
                '}';
    }
}