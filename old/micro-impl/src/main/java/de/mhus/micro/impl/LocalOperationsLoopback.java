package de.mhus.micro.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroExecutor;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroPusher;
import de.mhus.osgi.api.MOsgi;

/**
 * This service is  loopback to provide locl operations to the local micro registry.
 * 
 * @author mikehummel
 *
 */
@Component(immediate = true, service = { MicroPusher.class, MicroDiscoverer.class, MicroExecutor.class,
        EventHandler.class }, property = { EventConstants.EVENT_TOPIC + "=" + "de/mhus/micro/api/*" })
public class LocalOperationsLoopback extends MLog implements MicroPusher, MicroDiscoverer, MicroExecutor, EventHandler {

    private Map<UUID, LocalOperation> operations = Collections.synchronizedMap(new HashMap<>());
    
    @Reference
    private OperationsAdmin api;
    
    @Activate
    public void doActivate(ComponentContext ctx) {
        MOsgi.runAfterActivation(ctx, c -> {
            reload();
        });
    }
    
    @Deactivate
    public void doDeactivate() {
        if (operations == null) return;
        operations.forEach((k,o) -> MicroUtil.firePushRemove(o.getDescription() ) );
        operations = null;
    }

    @Override
    public void handleEvent(Event event) {
        if (operations == null) return;
        
        OperationDescription desc = (OperationDescription) event.getProperty(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        
        String topic = event.getTopic();
        log().i("event",event); //XXX
        if (OperationsAdmin.EVENT_TOPIC_ADD.equals(topic)) {
            OperationDescription desc2 = new OperationDescription(desc);
            desc2.putLabel(MicroConst.DESC_LABEL_TRANSPORT_TYPE, MicroConst.LOCAL_TRANSPORT);
            MicroUtil.firePushAdd(desc2);
        } else
        if (OperationsAdmin.EVENT_TOPIC_REMOVE.equals(topic)) {
            OperationDescription desc2 = new OperationDescription(desc);
            desc2.putLabel(MicroConst.DESC_LABEL_TRANSPORT_TYPE, MicroConst.LOCAL_TRANSPORT);
            MicroUtil.firePushRemove(desc2);
        } else
        if (MicroPusher.EVENT_TOPIC_ADD.equals(topic)) {
            if (MicroConst.LOCAL_TRANSPORT.equals(desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE))) {
                Operation oper = api.getOperation(desc.getUuid());
                if (oper == null) {
                    log().e("Operation event without operation",oper);
                } else {
                    operations.put(desc.getUuid(), new LocalOperation(desc, oper ));
                }
            }
        } else
        if (MicroPusher.EVENT_TOPIC_REMOVE.equals(topic)) {
            if (MicroConst.LOCAL_TRANSPORT.equals(desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE))) {
                operations.remove(desc.getUuid());
            }
        }
    }

    @Override
    public void find(MicroFilter filter, List<MicroOperation> results) {
        if (operations == null) return;
        
        operations.forEach((id,o) -> {
            if (filter.matches(o.getDescription()))
                results.add(o);
        });
    }

    @Override
    public void discover(MicroFilter filter, List<OperationDescription> results) {
        if (operations == null) return;
        
        operations.forEach((id,o) -> {
            if (filter.matches(o.getDescription()))
                results.add(o.getDescription());
        });
    }

    @Override
    public void reload() {
        if (operations == null) return;
        ArrayList<Operation> list = new ArrayList<>();
        api.list(list);
        list.forEach(o -> {
            OperationDescription desc2 = new OperationDescription(o.getDescription());
            desc2.putLabel(MicroConst.DESC_LABEL_TRANSPORT_TYPE, MicroConst.LOCAL_TRANSPORT);
            MicroUtil.firePushAdd(desc2);
        });
    }

}
