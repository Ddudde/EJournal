package ru.mirea.data.models.school;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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