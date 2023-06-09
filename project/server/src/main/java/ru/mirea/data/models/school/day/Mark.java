package ru.mirea.data.models.school.day;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long usr, userInv, period;

    private int mark, weight;

    private String type, style;

    private float avg;

    public Mark(Long usr, Long period, int mark, int weight, String type, String style) {
        this.usr = usr;
        this.period = period;
        this.mark = mark;
        this.weight = weight;
        this.type = type;
        this.style = style;
    }

    public Mark(Long usr, Long period, int weight, String type, float avg) {
        this.usr = usr;
        this.period = period;
        this.weight = weight;
        this.type = type;
        this.avg = avg;
    }
}