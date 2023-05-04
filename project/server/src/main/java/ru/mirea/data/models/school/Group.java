package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.ListLongConverter;
import ru.mirea.data.MapLongConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity(name = "grp") public class Group {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    @Convert(converter = ListLongConverter.class)
    @Column(columnDefinition="CLOB")
    private List<Long> kids, kidsInv;

    @Convert(converter = MapLongConverter.class)
    @Column(columnDefinition="CLOB")
    private Map<Long, Long> daysOfWeek;

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, List<Long> kids) {
        this.name = name;
        this.kids = kids;
    }

    public Group(String name, List<Long> kids, Map<Long, Long> daysOfWeek) {
        this.name = name;
        this.kids = kids;
        this.daysOfWeek = daysOfWeek;
    }

    public Map<Long, Long> getDaysOfWeek() {
        if(daysOfWeek == null) daysOfWeek = new HashMap<>();
        return daysOfWeek;
    }

    public List<Long> getKids() {
        if(kids == null) kids = new ArrayList<>();
        return kids;
    }

    public List<Long> getKidsInv() {
        if(kidsInv == null) kidsInv = new ArrayList<>();
        return kidsInv;
    }
}