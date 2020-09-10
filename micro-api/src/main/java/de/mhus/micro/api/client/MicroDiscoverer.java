package de.mhus.micro.api.client;

import java.util.List;

import org.osgi.service.event.EventConstants;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroDiscoverer {

    /**
     * Remove operation description.
     */
    String EVENT_TOPIC_REMOVE = "de/mhus/micro/api/client/MicroDiscoverer/remove";
    /**
     * Add or update operation description.
     */
    String EVENT_TOPIC_ADD = "de/mhus/micro/api/client/MicroDiscoverer/add";
    String EVENT_TOPICS = EventConstants.EVENT_TOPIC + "=" + "de/mhus/micro/api/client/MicroDiscoverer/*";
    String EVENT_PROPERTY_DESCRIPTION = "description";

    void discover(MicroFilter filter, List<OperationDescription> results);

}
