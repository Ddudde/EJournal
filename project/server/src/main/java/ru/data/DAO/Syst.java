package ru.data.DAO;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.data.DAO.auth.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class Syst {
    private String testPassword;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    @JoinColumn(name = "syst_adm_id")
    private List<User> admins;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "system_id")
    private List<News> news;

    @OneToOne(orphanRemoval = true)
    private Contacts contacts;

    public Syst(List<News> news, Contacts contacts) {
        this.news = new ArrayList<>(news);
        this.contacts = contacts;
    }

    public List<User> getAdmins() {
        if(admins == null) admins = new ArrayList<>();
        return admins;
    }

    public List<News> getNews() {
        if(news == null) news = new ArrayList<>();
        return news;
    }
}
