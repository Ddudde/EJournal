package ru.data.DAO.school;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Request {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    private String email, date;

    @Column(columnDefinition="text")
    private String fio;

    public Request(String email, String date, String fio) {
        this.email = email;
        this.date = date;
        this.fio = fio;
    }
}
