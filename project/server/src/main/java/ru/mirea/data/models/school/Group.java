package ru.mirea.data.models.school;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;
import ru.mirea.data.converters.MapLongConverter;
import ru.mirea.data.json.Role;

import javax.persistence.*;
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

    @Column(name = "name")
    private String name;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "kids")
    private List<Long> kids;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "kidsInv")
    private List<Long> kidsInv;

    @Convert(converter = MapLongConverter.class)
    @Column(name = "daysOfWeek")
    private Map<Long, Long> daysOfWeek;

    public Map<Long, Long> getDaysOfWeek() {
        if(daysOfWeek == null) {
            daysOfWeek = new HashMap<>();
        }
        return daysOfWeek;
    }

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
}