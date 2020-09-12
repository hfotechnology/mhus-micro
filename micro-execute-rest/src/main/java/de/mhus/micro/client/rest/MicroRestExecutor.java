package de.mhus.micro.client.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.io.http.MHttpClientBuilder;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroExecutor;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.osgi.api.MOsgi;

@Component(immediate = true, property = {
        MicroDiscoverer.EVENT_TOPICS
        },
        service = {MicroExecutor.class, EventHandler.class}
        )
public class MicroRestExecutor extends MLog implements MicroExecutor, EventHandler {

    private HttpClient client = new MHttpClientBuilder().getHttpClient();
    private Map<UUID,RestOperation> operations = Collections.synchronizedMap(new HashMap<>());

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
        if (!MicroConst.REST_TRANSPORT.equals(transType)) return;
//        String url = descParam.get(MicroConst.REST_URL);
//        if (url == null) return;
        
        if (event.getTopic().equals(MicroDiscoverer.EVENT_TOPIC_ADD)) {
            RestOperation oper = new RestOperation(desc, client);
            operations.put(desc.getUuid(), oper);
            MicroUtil.fireOperationAdd(oper);
        } else
        if (event.getTopic().equals(MicroDiscoverer.EVENT_TOPIC_REMOVE)) {
            RestOperation oper = operations.remove(desc.getUuid());
            if (oper != null)
                MicroUtil.fireOperationRemove(oper);
        }
    }

    @Override
    public void reload() {
        api = M.l(MicroApi.class);
        ArrayList<OperationDescription> list = new ArrayList<>();
        api.discover(new FilterTransport(MicroConst.REST_TRANSPORT), list);
        for (OperationDescription desc : list) {
            RestOperation oper = new RestOperation(desc, client);
            operations.put(desc.getUuid(), oper);
            MicroUtil.fireOperationAdd(oper);
        }
    }


}
