package ru.data.DAO.school;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name, dateN, dateK;

    public Period(String name, String dateN, String dateK) {
        this.name = name;
        this.dateN = dateN;
        this.dateK = dateK;
    }
}