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

    @Column(name = "usrInv")
    private Long userInv;

    @Column(name = "mark")
    private int mark;

    @Column(name = "weight")
    private int weight;

    @Column(name = "type")
    private String type;
}