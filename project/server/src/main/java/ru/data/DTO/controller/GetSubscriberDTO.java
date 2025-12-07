package ru.data.DTO.controller;

import lombok.AllArgsConstructor;
import ru.data.DTO.SubscriberDTO;

import java.util.UUID;

@AllArgsConstructor
public class GetSubscriberDTO {
    public UUID uuid;
    public SubscriberDTO subscriber;
}
