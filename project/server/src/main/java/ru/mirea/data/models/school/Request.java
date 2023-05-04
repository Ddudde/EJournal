package ru.mirea.data.models.school;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Request {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String email, date;

    @Column(columnDefinition="CLOB")
    private String text;//FIO

    public Request(String email, String date, String text) {
        this.email = email;
        this.date = date;
        this.text = text;
    }
}
