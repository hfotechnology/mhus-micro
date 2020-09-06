package de.mhus.micro.api.client;

import java.util.List;

import org.osgi.service.blueprint.container.EventConstants;

public interface MicroExecutor {
    
    String EVENT_PROPERTY_OPERATION = "operation";
    String EVENT_TOPIC_ADD = "de.mhus.micro.api.client.MicroExecutor/add";
    String EVENT_TOPIC_REMOVE = "de.mhus.micro.api.client.MicroExecutor/remove";
    String EVENT_TOPICS = EventConstants.EVENT + "=" + "de.mhus.micro.api.client.MicroExecutor/*";

    void list(MicroFilter filter, List<MicroOperation> results);

}
