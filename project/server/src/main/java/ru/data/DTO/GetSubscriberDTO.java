package ru.data.DTO;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class GetSubscriberDTO {
    public UUID uuid;
    public SubscriberDTO subscriber;
}
