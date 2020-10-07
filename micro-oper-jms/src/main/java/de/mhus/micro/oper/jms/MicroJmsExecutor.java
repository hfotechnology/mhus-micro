package de.mhus.micro.oper.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroExecutor;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.client.TransportFilter;
import de.mhus.osgi.api.MOsgi;

@Component(immediate = true, property = {
        MicroDiscoverer.EVENT_TOPICS
        },
        service = {MicroExecutor.class, EventHandler.class}
        )
public class MicroJmsExecutor extends MLog implements MicroExecutor, EventHandler {

    private Map<UUID,JmsOperation> operations = Collections.synchronizedMap(new HashMap<>());

    @Reference
    private MicroApi api;
    
    @Activate
    public void doActivate(ComponentContext ctx) {
        MOsgi.runAfterActivation(ctx, c -> doStart());
    }
    
    private void doStart() {
        reload();
    }

    @Override
    public void find(MicroFilter filter, List<MicroOperation> results) {
        operations.forEach((i,o) -> {
            if (filter.matches(o.getDescription()))
                results.add(o);
        });
    }

    @Override
    public void handleEvent(Event event) {
        
        OperationDescription desc = (OperationDescription)event.getProperty(MicroDiscoverer.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        HashMap<String, String> descParam = desc.getLabels();
        String transType = descParam.get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
        log().i("event",event); //XXX
        if (!AbstractOperationsChannel.TRANSPORT_JMS.equals(transType)) return;
        
        if (event.getTopic().equals(MicroDiscoverer.EVENT_TOPIC_ADD)) {
            JmsOperation oper = new JmsOperation(desc);
            operations.put(desc.getUuid(), oper);
            MicroUtil.fireOperationAdd(oper);
        } else
        if (event.getTopic().equals(MicroDiscoverer.EVENT_TOPIC_REMOVE)) {
            JmsOperation oper = operations.remove(desc.getUuid());
            if (oper != null)
                MicroUtil.fireOperationRemove(oper);
        }
    }

    @Override
    public void reload() {
        api = M.l(MicroApi.class);
        ArrayList<OperationDescription> list = new ArrayList<>();
        api.discover(new TransportFilter(AbstractOperationsChannel.TRANSPORT_JMS), list);
        for (OperationDescription desc : list) {
            JmsOperation oper = new JmsOperation(desc);
            operations.put(desc.getUuid(), oper);
            MicroUtil.fireOperationAdd(oper);
        }
    }

}
