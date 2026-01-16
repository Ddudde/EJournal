package ru.data.DTO.controller.school.analytics.schedule;

public class ScheduleInnerBodyDTO {
    public final String name;
    public final String cabinet;
    public final ScheduleInnerBodyDTO prepod;
    public final Long id;
    public String group;

    public ScheduleInnerBodyDTO(String name, String cabinet, ScheduleInnerBodyDTO prepod, Long id) {
        this.name = name;
        this.cabinet = cabinet;
        this.prepod = prepod;
        this.id = id;
    }
}
