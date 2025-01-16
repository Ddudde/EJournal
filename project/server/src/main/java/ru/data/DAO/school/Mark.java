package ru.data.DAO.school;

import lombok.*;
import ru.data.DAO.auth.User;

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
    private Period period;

    private int weight;

    private String type, style, mark;

    public Mark(User usr, Period period, String mark, int weight, String type, String style) {
        this.usr = usr;
        this.period = period;
        this.mark = mark;
        this.weight = weight;
        this.type = type;
        this.style = style;
    }
}