package de.mhus.micro.core.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.util.AbstractDiscovery;

public class RedisDiscovery extends AbstractDiscovery {

    public final static String PREFIX = "de.mhus.micro.core.redis.";
    public final static String SEARCH = PREFIX + "*";
    public static final String DATA_UUID = "uuid";
    public static final String DATA_PATH = "path";
    public static final String DATA_VERSION = "version";
    public static final String DATA_TITLE = "title";
    public static final String DATA_LABEL = "label.";
    public static final String DATA_FORM = "form";
    private static final String LABEL_DEPRECATED = "_deprecated";
    private static final String LABEL_NEW = "_new";
    
    private volatile Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());

    private JedisCon connection = null;

    public JedisCon getConnection() {
        return connection;
    }

    public void setConnection(JedisCon connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void discover() {
        descriptions.forEach((k,v) -> v.putLabel(LABEL_DEPRECATED, "true") );
        for (String name : connection.keys(SEARCH))
            load(name);
        descriptions.values().removeIf(v -> v.getLabels().containsKey(LABEL_DEPRECATED));
        descriptions.values().forEach(v -> {if (v.getLabels().containsKey(LABEL_NEW)) {
            api.updateDescription(v);
        }});
    }

    private void load(String name) {
        Map<String, String> data = connection.hgetAll(name);
        OperationDescription cur = descriptions.get(name);

        if (!data.containsKey(DATA_PATH)) {
            log().d("ignore entry $1 without path",name);
        }
        
        // path
        String path = data.get(DATA_PATH);

        // uuid
        UUID uuid = null;
        if (data.containsKey(DATA_UUID))
            uuid = UUID.fromString(data.get(DATA_UUID));
        else if (cur != null)
            uuid = cur.getUuid();
        else
            uuid = UUID.randomUUID();
        
        // version
        Version version = null;
        if (data.containsKey(DATA_VERSION))
            version = new Version(data.get(DATA_VERSION));
        else
            version = Version.V_0_0_0;
        
        // title
        String title = data.getOrDefault(DATA_TITLE, path);

        // create description
        OperationDescription desc = null;
        boolean isNew = false;
        if (cur == null || !path.equals(cur.getPath()) || !version.equals(cur.getVersion()) || !title.equals(cur.getTitle()) || !uuid.equals(cur.getUuid()) ) {
            desc = new OperationDescription(uuid, path, version, null, title);
            isNew = true;
        } else
            desc = cur;

        // labels
         desc.setLabels(MCollection.subsetCrop(DATA_LABEL, data));
         if (isNew) {
             desc.getLabels().put(LABEL_NEW, "true");
         }
         desc.getLabels().remove(LABEL_DEPRECATED);

        // out of definition: form
        if (data.containsKey(DATA_FORM)) {
            String formStr = data.get(DATA_FORM);
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
