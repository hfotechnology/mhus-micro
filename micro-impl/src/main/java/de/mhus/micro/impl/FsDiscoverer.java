package de.mhus.micro.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgFile;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;

@ServiceComponent
public class FsDiscoverer extends MLog implements MicroDiscoverer {

//    private static CfgFile CFG_DIR = new CfgFile(FsDiscoverer.class, "directory", MApi.getFile(MApi.SCOPE.DATA, "micro_discovery") );
    private static CfgFile CFG_DIR = new CfgFile(FsDiscoverer.class, "directory", new File("micro_discovery") );
    private static CfgTimeInterval CFG_REFRESH_PERIOD = new CfgTimeInterval(FsDiscoverer.class, "refreshPeriod", "5s");
    
    private Map<String,Object[]> descriptions = Collections.synchronizedMap(new HashMap<>());

    private long lastRefresh = 0;
    
    @ServiceActivate
    public void doActivate() {
        log().i("Start",CFG_DIR.value());
        refresh();
    }

    @ServiceDeactivate
    public void doDeactivate() {
        if (descriptions == null) return;
        log().i("Stop",CFG_DIR.value());
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
            if (!CFG_DIR.value().exists())
                return;
            for (File file : CFG_DIR.value().listFiles()) {
                log().t("File",file);
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String key = MFile.getFileNameOnly(file.getName());
                    Object[] entry = descriptions.get(key);
                    if (entry != null) {
                        Long modified = (Long)entry[0];
                        if (file.lastModified() != modified) {
                            load(file);
                        }
                    } else
                        load(file);
                }
            }
            for (String key : new ArrayList<>(descriptions.keySet())) {
                Object[] val = descriptions.get(key);
                if (val != null) {
                    File file = new File(CFG_DIR.value(), (String)val[2]);
                    if (!file.exists()) {
                        log().i("Removed", file);
                        // remove from list
                        descriptions.remove(key);
                        
                        // fire event
                        MicroUtil.fireOperationDescriptionRemove((OperationDescription) val[1]);
        
                    }
                }
            }
        } catch (Throwable t) {
            log().w(t);
        }
    }
    
    private void load(File file) {
        log().i("Add",file);
        try {
            JsonNode json = MJson.load(file);
            OperationDescription desc = OperationDescription.fromJson(json);
            
            // add to registry
            descriptions.put(desc.getUuid() + "_" + desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE), new Object[] { file.lastModified(), desc, file.getName() });
            
            // fire event
            MicroUtil.fireOperationDescriptionAdd(desc);
            
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
