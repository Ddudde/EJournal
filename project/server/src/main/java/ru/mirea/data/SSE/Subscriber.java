package ru.mirea.data.SSE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.FluxSink;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Subscriber {

    private String login;

    private TypesConnect type;

    private String podTypeL1;

    private String podTypeL2;

    private FluxSink<ServerSentEvent> fluxSink;

    public Subscriber(FluxSink<ServerSentEvent> fluxSink) {
        this.fluxSink = fluxSink;
    }
}