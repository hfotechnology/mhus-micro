package de.mhus.micro.core.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.impl.AbstractDiscovery;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDiscovery extends AbstractDiscovery {

    public final static String PREFIX = "de.mhus.micro.core.redis.";
    public final static String SEARCH = PREFIX + "*";
    
    private volatile Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());

    private JedisPool pool = null;

    public JedisPool getPool() {
        return pool;
    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public synchronized void reload() {
    	try (Jedis con = pool.getResource()) {
	        descriptions.forEach((k,v) -> ((IProperties)v.getLabels()).put(C.LABEL_DEPRECATED, "true") );
	        for (String name : con.keys(SEARCH))
	            load(name);
	        descriptions.values().removeIf(v -> v.getLabels().containsKey(C.LABEL_DEPRECATED));
    	}
    }

    private void load(String name) {
    	try (Jedis con = pool.getResource()) {
	        Map<String, String> data = con.hgetAll(name);
	        OperationDescription cur = descriptions.get(name);
	
	        if (!data.containsKey(C.DATA_PATH)) {
	            log().d("@Ignore entry $1 without path",name);
	        }
	        
	        // path
	        String path = data.get(C.DATA_PATH);
	
	        // uuid
	        UUID uuid = null;
	        if (data.containsKey(C.DATA_UUID))
	            uuid = UUID.fromString(data.get(C.DATA_UUID));
	        else if (cur != null)
	            uuid = cur.getUuid();
	        else
	            uuid = UUID.randomUUID();
	        
	        // version
	        Version version = null;
	        if (data.containsKey(C.DATA_VERSION))
	            version = new Version(data.get(C.DATA_VERSION));
	        else
	            version = Version.V_0_0_0;
	        
	        // title
	        String title = data.getOrDefault(C.DATA_TITLE, path);
	
	        // form
	        DefRoot form = null;
	        if (data.containsKey(C.DATA_FORM)) {
	            String formStr = data.get(C.DATA_FORM);
	            try {
	                form = ModelUtil.fromJson(formStr);
	            } catch (Throwable t) {
	                log().d("@Form of the description $1 is malformed",name,t);
	            }
	        }

	        // create description
	        OperationDescription desc = null;
	        if (cur == null || !path.equals(cur.getPath()) || !version.equals(cur.getVersion()) || !title.equals(cur.getTitle()) || !uuid.equals(cur.getUuid()) ) {
	            desc = new OperationDescription(uuid, path, version, null, title, null, form);
	        } else
	            desc = cur;
	
	        // labels
	         ((MProperties)desc.getLabels()).clear();
	         ((MProperties)desc.getLabels()).putReadProperties( IProperties.subsetCrop(C.DATA_LABEL_DOT, data));
	         IProperties.updateFunctional((MProperties)desc.getLabels());
	         
	         ((MProperties)desc.getLabels()).remove(C.LABEL_DEPRECATED);
	         // desc.getLabels().put(C.LABEL_DISCOVER_SOURCE, source);
	
	        
	        descriptions.put(name, desc);
    	}
    }
    
    @Override
    public Boolean discover(Function<OperationDescription,Boolean> action) {
		for ( OperationDescription desc : descriptions.values())
			if (!action.apply(desc) )
				return Boolean.FALSE;
		return Boolean.TRUE;
    }

	@Override
	public void check() {
		// nothing to do
	}

}
