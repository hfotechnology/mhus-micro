package de.mhus.micro.client.redis;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.MLog;
import de.mhus.micro.api.server.MicroPusher;

@Component(immediate = true, property = {
        MicroPusher.EVENT_TOPICS
        },
        service = {MicroPusher.class, EventHandler.class}
        )
public class RedisPusher extends MLog implements MicroPusher, EventHandler {

    @Override
    public void handleEvent(Event event) {
        
    }

}
