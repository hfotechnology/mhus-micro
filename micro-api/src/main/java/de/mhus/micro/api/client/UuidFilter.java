package de.mhus.micro.api.client;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.operation.OperationDescription;

public class UuidFilter implements MicroFilter {

    private UUID uuid;
    private IProperties labels;

    public UuidFilter(UUID uuid) {
        this.uuid = uuid;
    }
    
    public UuidFilter(UUID uuid, IProperties labels) {
        this.uuid = uuid;
        this.labels = labels;
    }
    
    @Override
    public boolean matches(OperationDescription desc) {
        boolean ret = uuid != null && uuid.equals(desc.getUuid());
        if (!ret) return false;
        if (labels == null) return true;
        HashMap<String, String> dLabels = desc.getLabels();
        if (dLabels == null) return labels.isEmpty();
        for (Entry<String, Object> entry : labels.entrySet()) {
            String dVal = dLabels.get(entry.getKey());
            if (dVal == null) return false;
            if (!entry.getValue().equals(dVal))
                return false;
        }
        return true;
    }

}
