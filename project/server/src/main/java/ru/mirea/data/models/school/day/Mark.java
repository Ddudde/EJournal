package ru.mirea.data.models.school.day;

import lombok.*;
import ru.mirea.data.models.auth.Invite;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Period;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User usr;

    @OneToOne
    private Invite userInv;

    @OneToOne
    private Period period;

    private int mark, weight;

    private String type, style;

    private Float avg;

    public Mark(User usr, Period period, int mark, int weight, String type, String style) {
        this.usr = usr;
        this.period = period;
        this.mark = mark;
        this.weight = weight;
        this.type = type;
        this.style = style;
    }

    public Mark(User usr, Period period, int weight, String type, Float avg) {
        this.usr = usr;
        this.period = period;
        this.weight = weight;
        this.type = type;
        this.avg = avg;
    }
}