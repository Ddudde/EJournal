package ru.mirea.data.models;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Contacts {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition="text")
    private String contact, text;

    private String imgUrl;

    public Contacts(String contact, String text, String imgUrl) {
        this.contact = contact;
        this.text = text;
        this.imgUrl = imgUrl;
    }
}