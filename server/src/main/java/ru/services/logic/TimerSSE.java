package ru.services.logic;

import ru.data.DTO.SubscriberDTO;

import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;

public class TimerSSE extends TimerTask {

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        SSEService.connectTokens.clear();
        for(Map.Entry<UUID, SubscriberDTO> sub: SSEService.subscriptions.entrySet()) {
            if(sub.getValue().getTimeOfExpired() > now) {
                SSEService.subscriptions.remove(sub.getKey());
            }
        }
    }
}
