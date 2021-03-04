package de.mhus.micro.oper.jms;

import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.annotations.service.ServiceReference;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.osgi.api.jms.JmsDataChannel;

@ServiceComponent(service = {JmsDataChannel.class,EventHandler.class,MicroProvider.class})
public class DefaultOperationsChannel extends AbstractOperationsChannel implements EventHandler, MicroProvider {

    private OperationsAdmin admin;

    @ServiceActivate
    public void doActivate() {
        reload();
    }
    
    @ServiceDeactivate
    public void doDeactivate() {
        clear();
    }
    
    @ServiceReference
    public void setAdmin(OperationsAdmin admin) {
        this.admin = admin;
    }
    
    @Override
    protected String getQueueName() {
        return MJms.getConfig().getStringOrCreate("queue", k -> MSystem.getHostname());
    }

    @Override
    public void provided(List<OperationDescription> list) {
        operations.values().forEach(v -> list.add( v.getDescription() ));
    }

    @Override
    public void reload() {
        admin = M.l(OperationsAdmin.class);
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
