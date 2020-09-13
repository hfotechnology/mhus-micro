package de.mhus.micro.client.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.micro.api.server.MicroPusher;

@Component(immediate = true,
        service = {MicroPusher.class, EventHandler.class},
        property = MicroPusher.EVENT_TOPICS
        )
public class RedisPusher extends MLog implements MicroPusher, EventHandler {

    private static CfgString CFG_PREFIX = new CfgString(RedisPusher.class, "prefix", MSystem.getHostname());

    private Map<String, OperationDescription> operations = Collections.synchronizedMap(new HashMap<>());

    @Reference
    private MicroApi api;

    @Reference
    private RedisAdmin redis;

    @Activate
    public void doActivate() {
        reload();
    }

    @Deactivate
    public void doDeactivate() {
        operations.forEach((k,d) -> remove(d) );
        operations = null;
    }

    @Override
    public void reload() {
        if (operations == null) return;
        api = M.l(MicroApi.class);
        if (api == null) return;
        
        List<OperationDescription> list = new LinkedList<>();
        for ( MicroProvider provider : api.getProviders()) {
            provider.provided(list);
        }
        list.forEach(desc -> {
            operations.put(ident(desc), desc);
            add(desc);
        });
        String prefix = CFG_PREFIX.value() + "_";
        try (JedisCon jedis = redis.getResource()) {
            for (String name : jedis.hkeys(redis.getNodeName())) {
                if (name.startsWith(prefix) && !operations.containsKey(name)) {
                    log().i("Remove from Redis",name);
                    jedis.hdel(redis.getNodeName(), name);
                }
                   
            }
        }
    }
    
    public static String ident(OperationDescription desc) {
        return CFG_PREFIX.value() + "_" + desc.getUuid() + "_" + desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
    }

    @Override
    public void handleEvent(Event event) {

        OperationDescription desc = (OperationDescription) event.getProperty(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        String transport = desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
        if (transport == null) return;
        
        String topic = event.getTopic();
        log().i("event",event); //XXX

        if (MicroPusher.EVENT_TOPIC_ADD.equals(topic)) {
                operations.put(ident(desc), desc);
                add(desc);
        } else
        if (MicroPusher.EVENT_TOPIC_REMOVE.equals(topic)) {
                operations.remove(ident(desc));
                remove(desc);
        }

    }

    private void remove(OperationDescription desc) {
        try (JedisCon jedis = redis.getResource()) {
            String ident = ident(desc);
            jedis.hdel(redis.getNodeName(), ident);
            jedis.publish(redis.getNodeName(), "remove:" + ident);
        } catch (Throwable t) {
            log().e(t);
        }
    }

    private void add(OperationDescription desc) {
        try (JedisCon jedis = redis.getResource()) {
            String content = toContent(desc);
            String ident = ident(desc);
            jedis.hset(redis.getNodeName(),ident, content);
            jedis.publish(redis.getNodeName(), "add:" + ident);
        } catch (Throwable t) {
            log().e(t);
        }
    }
    
    private String toContent(OperationDescription desc) throws Exception {
        ObjectNode json = desc.toJson();
        return MJson.toPrettyString(json);
    }
    

}
