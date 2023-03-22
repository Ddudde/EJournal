package ru.mirea.data.json;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Role implements Serializable {
    private String email;
    private Long YO;
    private Long group;
    private List<Long> kids;

    private List<Long> kidsInv;
    private List<Long> parents;

    private List<Long> parentsInv;
    private List<Long> subjects;

    public Role(String email, Long YO, Long group, List<Long> parents) { // kid
        this.email = email;
        this.YO = YO;
        this.group = group;
        this.parents = parents;
    }

    public Role(String email, Long YO, List<Long> kids) { // par
        this.email = email;
        this.YO = YO;
        this.kids = kids;
    }

    public Role(String email, List<Long> subjects, Long YO) { // tea
        this.email = email;
        this.YO = YO;
        this.subjects = subjects;
    }

    public Role(String email, Long YO) { //zav
        this.email = email;
        this.YO = YO;
    }

    public Role(String email) { //adm
        this.email = email;
    }
}
