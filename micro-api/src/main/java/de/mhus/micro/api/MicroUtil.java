package de.mhus.micro.api;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.mhus.lib.core.M;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroExecutor;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.server.MicroPusher;

public class MicroUtil {

    private static final Log log = Log.getLog(MicroUtil.class);
    
    public static void fireOperationDescriptionAdd(OperationDescription desc) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("fireOperationDescriptionAdd: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroDiscoverer.EVENT_PROPERTY_DESCRIPTION, desc);
        eventAdmin.postEvent(new Event(MicroDiscoverer.EVENT_TOPIC_ADD, prop));
    }

    public static void fireOperationDescriptionRemove(OperationDescription desc) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("fireOperationDescriptionRemove: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroDiscoverer.EVENT_PROPERTY_DESCRIPTION, desc);
        eventAdmin.postEvent(new Event(MicroDiscoverer.EVENT_TOPIC_REMOVE, prop));
    }

    public static void fireOperationAdd(MicroOperation oper) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("fireOperationAdd: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroExecutor.EVENT_PROPERTY_OPERATION, oper);
        eventAdmin.postEvent(new Event(MicroExecutor.EVENT_TOPIC_ADD, prop));
    }

    public static void fireOperationRemove(MicroOperation oper) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("fireOperationRemove: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroExecutor.EVENT_PROPERTY_OPERATION, oper);
        eventAdmin.postEvent(new Event(MicroExecutor.EVENT_TOPIC_REMOVE, prop));
    }

    public static void firePushAdd(OperationDescription desc) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("firePushAdd: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroPusher.EVENT_PROPERTY_DESCRIPTION, desc);
        eventAdmin.postEvent(new Event(MicroPusher.EVENT_TOPIC_ADD, prop));
    }

    public static void firePushRemove(OperationDescription desc) {
        EventAdmin eventAdmin = M.l(EventAdmin.class);
        if (eventAdmin == null) {
            log.w("firePushRemove: EventAdmin not found");
            return;
        }
        Map<String, Object> prop = new HashMap<>();
        prop.put(MicroPusher.EVENT_PROPERTY_DESCRIPTION, desc);
        eventAdmin.postEvent(new Event(MicroPusher.EVENT_TOPIC_REMOVE, prop));
    }
    
}

