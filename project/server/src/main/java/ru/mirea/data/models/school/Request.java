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

    @Column(columnDefinition="text")
    private String fio;

    public Request(String email, String date, String fio) {
        this.email = email;
        this.date = date;
        this.fio = fio;
    }
}
