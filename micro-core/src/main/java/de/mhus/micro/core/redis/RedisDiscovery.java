package de.mhus.micro.core.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.api.MicroFilter;
import de.mhus.micro.core.util.AbstractDiscovery;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDiscovery extends AbstractDiscovery {

    public final static String PREFIX = "de.mhus.micro.core.redis.";
    public final static String SEARCH = PREFIX + "*";
    private static final String LABEL_DEPRECATED = "_deprecated";
    private static final String LABEL_NEW = "_new";
    
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
	        descriptions.forEach((k,v) -> v.putLabel(LABEL_DEPRECATED, "true") );
	        for (String name : con.keys(SEARCH))
	            load(name);
	        descriptions.values().removeIf(v -> v.getLabels().containsKey(LABEL_DEPRECATED));
	        descriptions.values().forEach(v -> {if (v.getLabels().containsKey(LABEL_NEW)) {
	            api.updateDescription(v);
	        }});
    	}
    }

    private void load(String name) {
    	try (Jedis con = pool.getResource()) {
	        Map<String, String> data = con.hgetAll(name);
	        OperationDescription cur = descriptions.get(name);
	
	        if (!data.containsKey(C.DATA_PATH)) {
	            log().d("ignore entry $1 without path",name);
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
	
	        // create description
	        OperationDescription desc = null;
	        boolean isNew = false;
	        if (cur == null || !path.equals(cur.getPath()) || !version.equals(cur.getVersion()) || !title.equals(cur.getTitle()) || !uuid.equals(cur.getUuid()) ) {
	            desc = new OperationDescription(uuid, path, version, null, title);
	            isNew = true;
	        } else
	            desc = cur;
	
	        // labels
	         desc.setLabels(MCollection.subsetCrop(C.DATA_LABEL, data));
	         if (isNew) {
	             desc.getLabels().put(LABEL_NEW, "true");
	         }
	         desc.getLabels().remove(LABEL_DEPRECATED);
	
	        // out of definition: form
	        if (data.containsKey(C.DATA_FORM)) {
	            String formStr = data.get(C.DATA_FORM);
	            try {
	                DefRoot form = ModelUtil.fromJson(formStr);
	                desc.setForm(form);
	            } catch (Throwable t) {
	                log().d("Form of the description $1 is malformed",name,t);
	            }
	        }
	        
	        descriptions.put(name, desc);
    	}
    }
    
    @Override
    public void discover(MicroFilter filter, Consumer<OperationDescription> action) {
        //refresh();
        for (OperationDescription desc : descriptions.values()) {
            if (filter.matches(desc))
            	try {
            		action.accept(desc);
            	} catch (Throwable t) {
            		log().e(desc,t);
            	}
        }
    }

	@Override
	public void check() {
		// nothing to do
	}

}
