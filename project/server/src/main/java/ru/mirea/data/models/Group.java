package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.List;

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

    @Convert(converter = ListLongConverter.class)
    @Column(name = "daysOfWeek")
    private List<Long> daysOfWeek;

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, List<Long> kids) {
        this.name = name;
        this.kids = kids;
    }

    public Group(String name, List<Long> kids, List<Long> daysOfWeek) {
        this.name = name;
        this.kids = kids;
        this.daysOfWeek = daysOfWeek;
    }
}