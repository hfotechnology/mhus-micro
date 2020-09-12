package de.mhus.micro.client.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;
import redis.clients.jedis.Jedis;

@Component
public class RedisDiscoverer extends MLog implements MicroDiscoverer {

    private static CfgTimeInterval CFG_REFRESH_PERIOD = new CfgTimeInterval(RedisDiscoverer.class, "refreshPeriod", "5s");
    
    private Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());

    private long lastRefresh = 0;

    @Reference
    private RedisAdmin redis;
    
    @Activate
    public void doActivate() {
        log().i("Start");
        refresh();
    }

    @Deactivate
    public void doDeactivate() {
        if (descriptions == null) return;
        log().i("Stop");
        for (OperationDescription  val : descriptions.values())
            MicroUtil.fireOperationDescriptionRemove( val );
        descriptions = null;
    }
    
    private void refresh() {
        if (descriptions == null) return;
        try {
            
            if (!CFG_REFRESH_PERIOD.isTimeOut(lastRefresh))
                return;
            lastRefresh = System.currentTimeMillis();
            
            log().t("refresh");

            try (Jedis jedis = redis.getResource()) {
                for (String name : jedis.hkeys(redis.getNodeName())) {
                    OperationDescription entry = descriptions.get(name);
                    if (entry != null) {
                        // TODO check changed
                    } else {
                        load(jedis, name);
                    }
                }
                
              for (String key : new ArrayList<>(descriptions.keySet())) {
                  OperationDescription val = descriptions.get(key);
                  if (val != null) {
                      if (!jedis.hexists(redis.getNodeName(), key)) {
                          log().i("Removed", key);
                          descriptions.remove(key);
                          MicroUtil.fireOperationDescriptionRemove((OperationDescription) val);
                      }
                  }
              }
            } catch (Throwable t) {
                log().e(t);
            }

        } catch (Throwable t) {
            log().w(t);
        }
    }

    private void load(Jedis jedis, String name) throws JsonProcessingException, IOException {
        log().i("Add",name);
        String value = jedis.hget(redis.getNodeName(), name);
        JsonNode json = MJson.load(value);
        OperationDescription desc = OperationDescription.fromJson(json);
        
        // add to registry
        descriptions.put(RedisPusher.ident(desc), desc);
        
        // fire event
        MicroUtil.fireOperationDescriptionAdd(desc);
        

    }

    @Override
    public void discover(MicroFilter filter, List<OperationDescription> results) {
        refresh();
        for (OperationDescription desc : descriptions.values()) {
            if (filter.matches(desc))
                results.add(desc);
        }
    }

}
