package ru.mirea.data.models.school.dayOfWeek;

import lombok.*;
import ru.mirea.data.MapLongConverter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity public class DayOfWeek {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Convert(converter = MapLongConverter.class)
    @Column(columnDefinition="CLOB")
    private Map<Long, Long> lessons;

    public DayOfWeek(Map<Long, Long> lessons) {
        this.lessons = lessons;
    }

    public Map<Long, Long> getLessons() {
        if(lessons == null) lessons = new HashMap<>();
        return lessons;
    }
}