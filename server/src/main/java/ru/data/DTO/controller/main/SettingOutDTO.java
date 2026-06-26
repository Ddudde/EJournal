package ru.data.DTO.controller.main;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public class SettingOutDTO {
    private final String error;
    private final Boolean checkbox_hints;
    private final Boolean checkbox_notify;
    private final Boolean checkbox_notify_sched;
    private final Boolean checkbox_notify_marks;
    private final Boolean checkbox_notify_yo;
    private final Boolean checkbox_notify_por;
    private final Boolean checkbox_notify_new_sch;

    public transient final HttpStatus status;
}
