package de.mhus.micro.api.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.event.EventConstants;

import de.mhus.lib.core.operation.Operation;

public interface OperationsAdmin {

    String EVENT_PROPERTY_DESCRIPTION = "description";
    String EVENT_TOPIC_REMOVE = "de/mhus/micro/api/client/OperationsAdmin/remove";
    String EVENT_TOPIC_ADD = "de/mhus/micro/api/client/OperationsAdmin/add";
    String EVENT_TOPICS = EventConstants.EVENT_TOPIC + "=" + "de/mhus/micro/api/client/OperationsAdmin/*";

    void list(List<Operation> results);
    
    default List<Operation> list() {
        ArrayList<Operation> results = new ArrayList<>();
        list(results);
        return results;
    }

    Operation getOperation(String path, String version);

    Operation getOperation(UUID uuid);
    
}
