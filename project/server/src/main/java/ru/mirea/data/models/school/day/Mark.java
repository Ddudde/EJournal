package ru.mirea.data.models.school.day;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Mark {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "usr")
    private Long user;

    private Long userInv;

    private int mark, weight;

    private String type;
}