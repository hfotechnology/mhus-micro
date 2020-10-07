package de.mhus.micro.oper.jms;

import java.util.List;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.service.IdentUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroProvider;

@ServiceComponent
public class DefaultOperationsChannel extends AbstractOperationsChannel implements EventHandler, MicroProvider {

    @Reference
    private OperationsAdmin admin;

    @ServiceActivate
    public void doActivate() {
        reload();
    }
    
    @ServiceDeactivate
    public void doDeactivate() {
        clear();
    }
    
    @Override
    protected String getQueueName() {
        try {
            return MJms.getConfig().getString("queue");
        } catch (MException e) {
            log().e(e);
        }
        return IdentUtil.getFullIdent();
    }

    @Override
    public void provided(List<OperationDescription> list) {
        operations.values().forEach(v -> list.add( v.getDescription() ));
    }

    @Override
    public void reload() {
        for (Operation item : admin.list())
            add(item);
    }

    @Override
    public void handleEvent(Event event) {
        OperationDescription desc = (OperationDescription) event.getProperty(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        
        String topic = event.getTopic();
        log().i("event",event); //XXX
        if (OperationsAdmin.EVENT_TOPIC_ADD.equals(topic)) {
            Operation oper = admin.getOperation(desc.getUuid());
            add(oper);
        } else
        if (OperationsAdmin.EVENT_TOPIC_REMOVE.equals(topic)) {
            remove(desc.getPath() + "/" + desc.getVersionString());
        }
    }

    
}
