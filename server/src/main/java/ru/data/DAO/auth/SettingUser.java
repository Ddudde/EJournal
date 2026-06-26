package ru.data.DAO.auth;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class SettingUser {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    private String secFr, info, email, emailCode, expDateEC;

    private Integer ico;//1,2,3

    private Boolean notif = false, nChangeShedule = false,
        nNewMarks = false, nNewNewsYO = false,
        nNewNewsPor = false, nNewReqSch = false, hints = true;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ElementCollection
    private Set<String> tokens, topics;

    public SettingUser(Integer ico) {
        this.ico = ico;
    }

    public Set<String> getTokens() {
        if(tokens == null) tokens = new HashSet<>();
        return tokens;
    }

    public Set<String> getTopics() {
        if(topics == null) topics = new HashSet<>();
        return topics;
    }

    public void setNotif(Boolean notif) {
        this.notif = notif;
    }

    public void setNNewNewsYO(Boolean nNewNewsYO) {
        this.nNewNewsYO = nNewNewsYO;
    }

    public void setNNewNewsPor(Boolean nNewNewsPor) {
        this.nNewNewsPor = nNewNewsPor;
    }
}