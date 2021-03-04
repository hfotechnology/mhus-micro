package de.mhus.micro.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.util.MapList;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.osgi.api.util.MServiceTracker;

@Component(immediate = true)
public class OperationsAdminImpl extends MLog implements OperationsAdmin {

    private MServiceTracker<Operation> tracker = null;
    private List<Operation> operations = Collections.synchronizedList(new LinkedList<Operation>());
    private Map<UUID,Operation> operationsById = Collections.synchronizedMap(new HashMap<UUID,Operation>());
    private MapList<String, Operation> operationsByPath = new MapList<>();

    @Activate
    public void doActivate(BundleContext ctx) {
        tracker = new MServiceTracker<Operation>(ctx, Operation.class) {

            @Override
            protected void removeService(ServiceReference<Operation> reference, Operation service) {
                operations.remove(service);
                UUID id = service.getUuid();
                if (id != null)
                    operationsById.remove(id);
                try {
                    synchronized (operationsByPath) {
                        operationsByPath.removeEntry(service.getDescription().getPath(), service);
                    }
                } catch (Throwable t) {}

                EventAdmin eventAdmin = M.l(EventAdmin.class);
                if (eventAdmin == null) {
                    log().d("removeService: EventAdmin not found");
                } else {
                    Map<String, Object> prop = new HashMap<>();
                    prop.put(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION, service.getDescription());
                    eventAdmin.postEvent(new Event(OperationsAdmin.EVENT_TOPIC_REMOVE, prop));
                }
            }

            @Override
            protected void addService(ServiceReference<Operation> reference, Operation service) {
                operations.add(service);
                UUID id = service.getUuid();
                if (id != null)
                    operationsById.put(id, service);
                try {
                    synchronized (operationsByPath) {
                        operationsByPath.putEntry(service.getDescription().getPath(), service);
                    }
                } catch (Throwable t) {}

                EventAdmin eventAdmin = M.l(EventAdmin.class);
                if (eventAdmin == null) {
                    log().d("removeService: EventAdmin not found");
                } else {
                    Map<String, Object> prop = new HashMap<>();
                    prop.put(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION, service.getDescription());
                    eventAdmin.postEvent(new Event(OperationsAdmin.EVENT_TOPIC_ADD, prop));
                }
            }
        };
        tracker.start();
    }
    
    @Deactivate
    public void doDeactivate() {
        if (tracker != null)
            tracker.stop();
        tracker = null;
    }

    @Override
    public void list(List<Operation> results) {
        operations.forEach(i -> results.add(i));
    }

    @Override
    public Operation getOperation(String path, String version) {
        synchronized (operationsByPath) {
            List<Operation> list = operationsByPath.get(path);
            if (MCollection.isEmpty(list)) return null;
            if (version == null) return list.get(0);
            VersionRange range = new VersionRange(version);
            for (Operation oper : list) {
                if (range.includes(oper.getDescription().getVersion()))
                    return oper;
            }
        }
        return null;
    }

    @Override
    public Operation getOperation(UUID uuid) {
        return operationsById.get(uuid);
    }

}
