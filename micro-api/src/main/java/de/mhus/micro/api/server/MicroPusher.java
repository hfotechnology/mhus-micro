package de.mhus.micro.api.server;

import org.osgi.service.event.EventConstants;

public interface MicroPusher {

    String EVENT_PROPERTY_DESCRIPTION = "description";
    String EVENT_TOPIC_REMOVE = "de.mhus.micro.api.server.MicroPusher/remove";
    String EVENT_TOPIC_ADD = "de.mhus.micro.api.server.MicroPusher/add";
    String EVENT_TOPICS = EventConstants.EVENT + "=" + "de.mhus.micro.api.server.MicroPusher/*";

}
