package ru.data.DAO.school;

import jakarta.persistence.*;
import lombok.*;
import ru.data.DAO.auth.User;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private User usr;

    @ManyToOne
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