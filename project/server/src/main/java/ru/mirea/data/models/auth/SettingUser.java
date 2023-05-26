package ru.mirea.data.models.auth;

import lombok.*;
import ru.mirea.Main;
import ru.mirea.data.SetStringConverter;
import ru.mirea.services.PushService;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class SettingUser {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String secFr, info;

    private Integer ico;

    private Boolean notif = false, nChangeShedule = false,
            nNewMarks = false, nNewNewsYO = false,
            nNewNewsPor = false, nNewReqSch = false, hints = true;

    @Convert(converter = SetStringConverter.class)
    @Column(columnDefinition="CLOB")
    private Set<String> tokens, topics;

    public SettingUser(Integer ico) {
        this.ico = ico;
    }

    public SettingUser(Integer ico, Set<String> topics) {
        this.ico = ico;
        this.topics = topics;
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
        PushService pushService = (PushService) Main.ctx.getBean("pushService");
        if(notif){
            getTopics().forEach((topic) -> {
                if(notif
                && ((topic.contains("News") && nNewNewsYO)
                || (topic.contains("news") && nNewNewsPor))) {
                    pushService.subscribe(new ArrayList<>(getTokens()), topic);
                }
            });
        } else {
            getTopics().forEach((topic) -> {
                pushService.unsubscribe(new ArrayList<>(getTokens()), topic);
            });
        }}

    public void setNChangeShedule(Boolean nChangeShedule) {
        this.nChangeShedule = nChangeShedule;
    }

    public void setNNewMarks(Boolean nNewMarks) {
        this.nNewMarks = nNewMarks;
    }

    public void setNNewNewsYO(Boolean nNewNewsYO) {
        this.nNewNewsYO = nNewNewsYO;
        changeSubscribe("News", nNewNewsYO);
    }

    public void setNNewNewsPor(Boolean nNewNewsPor) {
        this.nNewNewsPor = nNewNewsPor;
        changeSubscribe("news", nNewNewsPor);
    }

    public void setNNewReqSch(Boolean nNewReqSch) {
        this.nNewReqSch = nNewReqSch;
    }

    private void changeSubscribe(String name, boolean val) {
        PushService pushService = (PushService) Main.ctx.getBean("pushService");
        if(val){
            getTopics().forEach((topic) -> {
                if(notif && topic.contains(name)) {
                    pushService.subscribe(new ArrayList<>(getTokens()), topic);
                }
            });
        } else {
            getTopics().forEach((topic) -> {
                if(!topic.contains(name)) {
                    pushService.unsubscribe(new ArrayList<>(getTokens()), topic);
                }
            });
        }
    }
}