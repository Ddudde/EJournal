package ru.data.DTO;

import lombok.AllArgsConstructor;
import ru.data.SSE.Subscriber;

import java.util.UUID;

@AllArgsConstructor
public class GetSubscriberDTO {
    public UUID uuid;
    public Subscriber subscriber;
}
