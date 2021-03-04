package de.mhus.micro.client.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgSecure;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.service.IdentUtil;
import redis.clients.jedis.JedisPool;

@Component(immediate = true)
public class RedisAdminImpl extends MLog implements RedisAdmin {

    public static CfgString CFG_REDIS_NODE = new CfgString(RedisAdmin.class, "redisNode", "de.mhus.micro.client.redis");
    public static CfgString CFG_REDIS_HOST = new CfgString(RedisAdmin.class, "redisHost", "redis");
    public static CfgInt CFG_REDIS_PORT = new CfgInt(RedisAdmin.class, "redisPort", 6379);
    public static CfgTimeInterval CFG_REDIS_TIMEOUT = new CfgTimeInterval(RedisAdmin.class, "redisTimeout", "5s");
    public static CfgSecure CFG_REDIS_PASSWORD = new CfgSecure(RedisAdmin.class, "redisPassword", (String)null);
    public static CfgInt CFG_REDIS_DATABASE = new CfgInt(RedisAdmin.class, "redisDatabase", 0);

    private JedisPool pool;
    private String nodeName;

    @Activate
    public void doActivate() {
        nodeName = CFG_REDIS_NODE.value();
        log().i("Start",nodeName);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(20);
        pool = new JedisPool(config,CFG_REDIS_HOST.value(), CFG_REDIS_PORT.value(), (int)CFG_REDIS_TIMEOUT.interval(), CFG_REDIS_PASSWORD.valueAsString(), CFG_REDIS_DATABASE.value(), IdentUtil.getFullIdent() );
    }

    @Deactivate
    public void doDeactivate() {
        log().i("Stop",nodeName);
        pool.destroy();
    }

    @Override
    public JedisCon getResource() {
        return new JedisCon(pool);
    }
    
    @Override
    public String getNodeName() {
        return nodeName;
    }
}
