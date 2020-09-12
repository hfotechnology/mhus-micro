package de.mhus.micro.client.redis;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgString;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component(immediate = true)
public class RedisAdminImpl extends MLog implements RedisAdmin {

    public static CfgString CFG_REDIS_NODE = new CfgString(RedisAdmin.class, "redisNode", "de.mhus.micro.client.redis");
    public static CfgString CFG_REDIS_HOST = new CfgString(RedisAdmin.class, "redisHost", "redis");
    public static CfgInt CFG_REDIS_PORT = new CfgInt(RedisAdmin.class, "redisPort", 6379);

    private JedisPool pool;
    private String nodeName;

    @Activate
    public void doActivate() {
        nodeName = CFG_REDIS_NODE.value();
        log().i("Start",nodeName);
        pool = new JedisPool(CFG_REDIS_HOST.value(), CFG_REDIS_PORT.value());
    }

    @Deactivate
    public void doDeactivate() {
        log().i("Stop",nodeName);
        pool.destroy();
    }

    @Override
    public Jedis getResource() {
        return pool.getResource();
    }
    
    @Override
    public String getNodeName() {
        return nodeName;
    }
}
