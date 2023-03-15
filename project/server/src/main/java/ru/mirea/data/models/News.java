package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.converters.RoleConverter;
import ru.mirea.data.json.Role;

import javax.persistence.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class News {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "date")
    private String date;

    @Column(name = "img_url")
    private String img_url;

    @Column(name = "text")
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