package de.mhus.micro.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgFile;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;

@ServiceComponent
public class FsDiscoverer extends MLog implements MicroDiscoverer {

    private static CfgFile CFG_DIR = new CfgFile(FsDiscoverer.class, "directory", MApi.getFile(MApi.SCOPE.DATA, "micro_discovery") );
    
    private Map<String,Object[]> descriptions = Collections.synchronizedMap(new HashMap<>());

    private Timer timer;
    
    @ServiceActivate
    public void doActivate() {
        log().i("Start",CFG_DIR.value());
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                try {
                    refresh();
                } catch (Throwable t) {
                    log().w(t);
                }
            }
        }, 5000, 5000);
    }
    
    @ServiceDeactivate
    public void doDeactivate() {
        log().i("Stop",CFG_DIR.value());
        if (timer != null)
            timer.cancel();
        timer = null;
    }
    
    private void refresh() {
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
                        loadFile(file);
                    }
                } else
                    loadFile(file);
            }
        }
        for (String key : new ArrayList<>(descriptions.keySet())) {
            File file = new File(CFG_DIR.value(), key + ".json");
            if (!file.exists()) {
                Object[] val = descriptions.get(key);
                log().i("Removed", file);
                // remove from list
                descriptions.remove(key);
                
                // fire event
                MicroUtil.fireOperationDescriptionRemove((OperationDescription) val[1]);

            }
        }
    }
    
    private void loadFile(File file) {
        log().i("Add",file);
        try {
            JsonNode json = MJson.load(file);
            UUID uuid = UUID.fromString(json.get("uuid").asText());
            String path = json.get("path").asText();
            String title = json.get("title").asText();
            Version version = new Version( json.get("version").asText() );
            
            OperationDescription desc = new OperationDescription(uuid, path, version, null, title);
            
            if (json.has("form")) {
                DefRoot form = ModelUtil.toModel((ObjectNode)json.get("form"));
                desc.setForm(form);
            }
            
            HashMap<String, String> labels = desc.getLabels();
            for (Map.Entry<String, JsonNode> field : M.iterate(json.get("labels").fields())) {
                labels.put(field.getKey(), field.getValue().asText());
            }
            
            // add to registry
            descriptions.put(uuid + "_" + labels.get(MicroConst.DESC_LABEL_TRANSPORT_TYPE), new Object[] { file.lastModified(), desc });
            
            // fire event
            MicroUtil.fireOperationDescriptionAdd(desc);
            
        } catch (Throwable t) {
            log().w(t);
        }
    }

    @Override
    public void discover(MicroFilter filter, List<OperationDescription> results) {
        for (Object[] val : descriptions.values()) {
            OperationDescription desc = (OperationDescription) val[1];
            if (filter.matches(desc))
                results.add(desc);
        }
    }

}
