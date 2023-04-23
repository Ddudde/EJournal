package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Syst {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "admins")
    private List<Long> admins;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "adminsInv")
    private List<Long> adminsInv;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "news")
    private List<Long> news;

    @Column(name = "contacts")
    private Long contacts;

    public Syst(List<Long> admins) {
        this.admins = admins;
    }

    public Syst(List<Long> admins, List<Long> news, Long contacts) {
        this.admins = admins;
        this.news = news;
        this.contacts = contacts;
    }

    public List<Long> getAdmins() {
        if(admins == null) {
            admins = new ArrayList<>(asList());
        }
        return admins;
    }

    public List<Long> getAdminsInv() {
        if(adminsInv == null) {
            adminsInv = new ArrayList<>(asList());
        }
        return adminsInv;
    }

    public List<Long> getNews() {
        if(news == null) {
            news = new ArrayList<>(asList());
        }
        return news;
    }
}
