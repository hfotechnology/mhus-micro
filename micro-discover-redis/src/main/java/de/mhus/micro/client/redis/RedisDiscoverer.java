package de.mhus.micro.client.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;

@Component
public class RedisDiscoverer extends MLog implements MicroDiscoverer {

    private static CfgTimeInterval CFG_REFRESH_PERIOD = new CfgTimeInterval(RedisDiscoverer.class, "refreshPeriod", "5s");
    
    private Map<String,Object[]> descriptions = Collections.synchronizedMap(new HashMap<>());

    private long lastRefresh = 0;
    
    @Activate
    public void doActivate() {
        log().i("Start");
        refresh();
    }

    @Deactivate
    public void doDeactivate() {
        if (descriptions == null) return;
        log().i("Stop");
        for (Object[]  val : descriptions.values())
            MicroUtil.fireOperationDescriptionRemove( ((OperationDescription)val[1]) );
        descriptions = null;
    }
    
    private void refresh() {
        if (descriptions == null) return;
        try {
            
            if (!CFG_REFRESH_PERIOD.isTimeOut(lastRefresh))
                return;
            lastRefresh = System.currentTimeMillis();
            
            log().t("refresh");

//            for (File file : CFG_DIR.value().listFiles()) {
//                log().t("File",file);
//                if (file.isFile() && file.getName().endsWith(".json")) {
//                    String key = MFile.getFileNameOnly(file.getName());
//                    Object[] entry = descriptions.get(key);
//                    if (entry != null) {
//                        Long modified = (Long)entry[0];
//                        if (file.lastModified() != modified) {
//                            load(file);
//                        }
//                    } else
//                        load(file);
//                }
//            }
//            for (String key : new ArrayList<>(descriptions.keySet())) {
//                Object[] val = descriptions.get(key);
//                if (val != null) {
//                    File file = new File(CFG_DIR.value(), (String)val[2]);
//                    if (!file.exists()) {
//                        log().i("Removed", file);
//                        // remove from list
//                        descriptions.remove(key);
//                        
//                        // fire event
//                        MicroUtil.fireOperationDescriptionRemove((OperationDescription) val[1]);
//        
//                    }
//                }
//            }
        } catch (Throwable t) {
            log().w(t);
        }
    }

    @Override
    public void discover(MicroFilter filter, List<OperationDescription> results) {
        refresh();
        for (Object[] val : descriptions.values()) {
            OperationDescription desc = (OperationDescription) val[1];
            if (filter.matches(desc))
                results.add(desc);
        }
    }

}
