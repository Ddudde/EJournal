package ru.mirea.data.models;

import lombok.*;
import ru.mirea.data.converters.ListLongConverter;

import javax.persistence.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class DayOfWeek {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Convert(converter = ListLongConverter.class)
    @Column(name = "lessons")
    private List<Long> lessons;

    public DayOfWeek(List<Long> lessons) {
        this.lessons = lessons;
    }
}