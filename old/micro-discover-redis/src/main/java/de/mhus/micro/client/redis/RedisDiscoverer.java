package de.mhus.micro.client.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.lib.core.service.TimerIfc;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;
import redis.clients.jedis.JedisPubSub;

@Component
public class RedisDiscoverer extends MLog implements MicroDiscoverer {

    private static CfgTimeInterval CFG_REFRESH_PERIOD = new CfgTimeInterval(RedisDiscoverer.class, "refreshPeriod", "120s");
    
    private volatile Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());

    private long lastRefresh = 0;

    @Reference
    private RedisAdmin redis;

    @Reference
    private TimerFactory timerFacory;

    private TimerIfc timer;

    private JedisCon jedisSubscribe;

    private JedisPubSub jedisPubSub;
    
    @Activate
    public void doActivate() {
        log().i("Start","RedisDiscoverer");
        
        jedisSubscribe = redis.getResource();
        jedisPubSub = new JedisPubSub() {

            @Override
            public void onMessage(String channel, String message) {
                try {
                    int pos = message.indexOf(":");
                    if (pos > 0) {
                        String action = message.substring(0,pos);
                        String key = message.substring(pos+1);
                        if (action.equals("add")) {
                            try (JedisCon jedis = redis.getResource()) {
                                load(jedis, key, true);
                            }
                        } else
                        if (action.equals("remove")) {
                            try (JedisCon jedis = redis.getResource()) {
                                remove(jedis, key);
                            }
                        }
                    }
                } catch (Throwable t) {
                    log().e("onMessage",channel,message,t);
                }
            }

            @Override
            public void onPMessage(String pattern, String channel, String message) {
                
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
                
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
                
            }
            
        };
        MThread.run(t -> jedisSubscribe.subscribe(jedisPubSub, redis.getNodeName()) );
        
        timer = timerFacory.getTimer();
        timer.schedule(getClass().getCanonicalName(), new TimerTask() {
            
            @Override
            public void run() {
                if (descriptions == null) {
                    cancel();
                    return;
                }
                refresh();
            }
        }, 100, CFG_REFRESH_PERIOD.interval());
        

    }

    @Deactivate
    public void doDeactivate() {
        if (descriptions == null) return;
        log().i("Stop","RedisDiscoverer");
        for (OperationDescription  val : descriptions.values())
            MicroUtil.fireOperationDescriptionRemove( val );
        timer.cancel();
        jedisPubSub.unsubscribe();
        jedisSubscribe.getClient().resetPipelinedCount(); // hack, if not jedisSubscribe.close will block
        jedisSubscribe.close();
        descriptions = null;
    }
    
    private void refresh() {
            
        if (!CFG_REFRESH_PERIOD.isTimeOut(lastRefresh))
            return;
        lastRefresh = System.currentTimeMillis();
        reload();
    }
    
    @Override
    public void reload() {
        if (descriptions == null) return;
        try {
            log().t("reload");

            try (JedisCon jedis = redis.getResource()) {
                for (String name : jedis.hkeys(redis.getNodeName())) {
                    OperationDescription entry = descriptions.get(name);
                    if (entry != null) {
                        // TODO check changed
                        load(jedis, name, false);
                    } else {
                        load(jedis, name, true);
                    }
                }
                
              for (String key : new ArrayList<>(descriptions.keySet())) {
                  remove(jedis, key);
              }
            } catch (Throwable t) {
                log().e(t);
            }

        } catch (Throwable t) {
            log().w(t);
        }
    }

    private void remove(JedisCon jedis, String key) {
        OperationDescription val = descriptions.get(key);
        if (val != null) {
            if (!jedis.hexists(redis.getNodeName(), key)) {
                log().i("Removed", key);
                descriptions.remove(key);
                MicroUtil.fireOperationDescriptionRemove(val);
            }
        }
    }

    private void load(JedisCon jedis, String name, boolean push) throws JsonProcessingException, IOException {
        if (push)
            log().i("Add",name);
        else
            log().i("Update",name);
        String value = jedis.hget(redis.getNodeName(), name);
        JsonNode json = MJson.load(value);
        long timeout = json.get("timeout").longValue();
        OperationDescription desc = OperationDescription.fromJson(json);
        if (timeout < System.currentTimeMillis()) {
            log().i("Add timeout",name);
            descriptions.remove(name);
            MicroUtil.fireOperationDescriptionRemove(desc);
            return;
        }
        
        // add to registry
        descriptions.put(name, desc);
        
        // fire event
        if (push)
            MicroUtil.fireOperationDescriptionAdd(desc);
        

    }

    @Override
    public void discover(MicroFilter filter, List<OperationDescription> results) {
        //refresh();
        for (OperationDescription desc : descriptions.values()) {
            if (filter.matches(desc))
                results.add(desc);
        }
    }

}
