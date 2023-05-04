package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.ListLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Syst {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Convert(converter = ListLongConverter.class)
    @Column(columnDefinition="CLOB")
    private List<Long> admins, adminsInv, news;

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
        if(admins == null) new ArrayList<>();
        return admins;
    }

    public List<Long> getAdminsInv() {
        if(adminsInv == null) adminsInv = new ArrayList<>();
        return adminsInv;
    }

    public List<Long> getNews() {
        if(news == null) news = new ArrayList<>();
        return news;
    }
}
