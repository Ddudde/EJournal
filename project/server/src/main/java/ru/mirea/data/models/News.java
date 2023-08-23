package ru.mirea.data.models;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class News {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String title, date, img_url;

    @Column(columnDefinition="text")
    private String text;

    public News(String title, String date, String text) {
        this.title = title;
        this.date = date;
        this.text = text;
    }

    public News(String title, String date, String img_url, String text) {
        this.title = title;
        this.date = date;
        this.img_url = img_url;
        this.text = text;
    }
}