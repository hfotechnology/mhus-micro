package de.mhus.micro.core.redis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.mhus.lib.core.MJson;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.util.AbstractPublisher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisPublisher extends AbstractPublisher {

    private JedisPool pool = null;

    public JedisPool getPool() {
        return pool;
    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void push(OperationDescription desc) {
        
    	Map<String,String> val = new HashMap<>();
    	
    	val.put(C.DATA_UUID, desc.getUuid().toString());
    	val.put(C.DATA_PATH, desc.getPath());
    	val.put(C.DATA_VERSION, desc.getVersionString());
    	val.put(C.DATA_TITLE, desc.getTitle());
    	for (Entry<String, String> label :  desc.getLabels().entrySet())
    	val.put(C.DATA_LABEL + label.getKey(), label.getValue());
    	DefRoot form = desc.getForm();
    	if (form != null) {
    		try {
	    		String formStr = MJson.toString( ModelUtil.toJson(form) );
	    		val.put(C.DATA_FORM, formStr);
    		} catch (IOException e) {
    			log().e(desc,e);
    		}
    	}
    	
    	String key = desc.getPath() + "-" + desc.getUuid() + "-" + desc.getLabels().getOrDefault(C.LABEL_TRANSPORT_TYPE, "");
    	try (Jedis con = pool.getResource()) {
    		con.hset(RedisDiscovery.PREFIX + key, val);
    	}
    }

}
