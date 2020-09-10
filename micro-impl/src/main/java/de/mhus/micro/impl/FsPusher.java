package de.mhus.micro.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceReference;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgFile;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.micro.api.server.MicroPusher;

@ServiceComponent(service = {MicroPusher.class, EventHandler.class},property = MicroPusher.EVENT_TOPICS)
public class FsPusher extends MLog implements MicroPusher, EventHandler {

    private static CfgFile CFG_DIR = new CfgFile(FsPusher.class, "directory", MApi.getFile(MApi.SCOPE.DATA, "micro_discovery") );
    
    private Map<String, OperationDescription> operations = Collections.synchronizedMap(new HashMap<>());
    
    private MicroApi api;
    
    @ServiceActivate
    public void doActivate() {
        reload();
    }

    @Override
    public void reload() {
        api = M.l(MicroApi.class);
        if (!CFG_DIR.value().exists())
            CFG_DIR.value().mkdirs();

        for (File file : CFG_DIR.value().listFiles())
            if (file.isFile() && file.getName().endsWith(".json"))
                file.delete();

        List<OperationDescription> list = new LinkedList<>();
        for ( MicroProvider provider : api.getProviders()) {
            provider.provided(list);
        }
        list.forEach(desc -> {
            operations.put(desc.getUuid() + "_" + desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE), desc);
            writeFile(desc);
        });
    }

    @Override
    public void handleEvent(Event event) {

        OperationDescription desc = (OperationDescription) event.getProperty(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        String transport = desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
        if (transport == null) return;
        
        String topic = event.getTopic();
        log().d("event", topic, desc);

        if (MicroPusher.EVENT_TOPIC_ADD.equals(topic)) {
                operations.put(desc.getUuid() + "_" + transport, desc);
                writeFile(desc);
        } else
        if (MicroPusher.EVENT_TOPIC_REMOVE.equals(topic)) {
                operations.remove(desc.getUuid() + "_" + transport);
                removeFile(desc);
        }

    }

    private void removeFile(OperationDescription desc) {
        if (!CFG_DIR.value().exists())
            CFG_DIR.value().mkdirs();
        String transport = desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
        File f = new File(CFG_DIR.value(), desc.getUuid() + "_" + transport + ".json");
        f.delete();
    }

    private void writeFile(OperationDescription desc) {
        if (!CFG_DIR.value().exists())
            CFG_DIR.value().mkdirs();
        String transport = desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE);
        File f = new File(CFG_DIR.value(), desc.getUuid() + "_" + transport + ".json");
        try {
            String content = toContent(desc);
            MFile.writeFile(f, content);
        } catch (Exception e) {
            log().e(f,e);
        }
    }

    private String toContent(OperationDescription desc) throws Exception {
        ObjectNode json = MJson.createObjectNode();

        json.put("path", desc.getPath());
        json.put("uuid", desc.getUuid().toString());
        json.put("title", desc.getTitle());
        json.put("version", desc.getVersionString());
        DefRoot form = desc.getForm();
        if (form != null) {
            form.build();
            ObjectNode formJson = ModelUtil.toJson(form);
            json.set("form", formJson);
        }
        ObjectNode labelsJson = MJson.createObjectNode();
        for (Entry<String, String> label : desc.getLabels().entrySet()) {
            labelsJson.put(label.getKey(), label.getValue());
        }
        json.set("labels", labelsJson);

        return MJson.toPrettyString(json);
    }

    public MicroApi getApi() {
        return api;
    }

    @ServiceReference
    public void setApi(MicroApi api) {
        this.api = api;
    }

}
