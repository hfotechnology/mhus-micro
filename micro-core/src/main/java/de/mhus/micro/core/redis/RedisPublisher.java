package de.mhus.micro.core.redis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.impl.AbstractPublisher;
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
        
//    	if (source.equals(desc.getLabels().get(C.LABEL_DISCOVER_SOURCE)))
//    		return;
    	
    	Map<String,String> val = new HashMap<>();
    	
    	val.put(C.DATA_UUID, desc.getUuid().toString());
    	val.put(C.DATA_PATH, desc.getPath());
    	val.put(C.DATA_VERSION, desc.getVersionString());
    	val.put(C.DATA_TITLE, desc.getTitle());
    	for (Entry<String, Object> label :  desc.getLabels().entrySet())
    		if (!label.getKey().startsWith(C.LABEL_LOCAL_PREFIX))
    			val.put(C.DATA_LABEL_DOT + label.getKey(), MCast.toString(label.getValue()));
    	DefRoot form = desc.getForm();
    	if (form != null) {
    		try {
	    		String formStr = MJson.toString( ModelUtil.toJson(form) );
	    		val.put(C.DATA_FORM, formStr);
    		} catch (IOException e) {
    			log().e(desc,e);
    		}
    	}
    	
    	String key = RedisDiscovery.PREFIX + C.getUniqueId(desc);
    	try (Jedis con = pool.getResource()) {
    		con.hset(key, val);
    	}
    }

	@Override
	public void remove(OperationDescription desc) {
		
//    	if (source.equals(desc.getLabels().get(C.LABEL_DISCOVER_SOURCE)))
//    		return;

    	String key = RedisDiscovery.PREFIX + C.getUniqueId(desc);
    	try (Jedis con = pool.getResource()) {
    		con.del(key);
    	}
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
