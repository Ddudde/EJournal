package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name, dateN, dateK;

    public Period(String name, String dateN, String dateK) {
        this.name = name;
        this.dateN = dateN;
        this.dateK = dateK;
    }
}