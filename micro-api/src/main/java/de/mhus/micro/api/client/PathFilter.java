package de.mhus.micro.api.client;

import java.util.HashMap;
import java.util.Map.Entry;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.VersionRange;

public class PathFilter implements MicroFilter {

    protected String path;
    private VersionRange range;
    private IProperties labels;

    public PathFilter(String path) {
        this.path = path;
    }
    
    public PathFilter(String path, String versionRange) {
        this.path = path;
        if (versionRange != null)
            this.range = new VersionRange(versionRange);
    }
    
    public PathFilter(String path, String versionRange, IProperties labels) {
        this.path = path;
        if (versionRange != null)
            this.range = new VersionRange(versionRange);
        this.labels = labels;
    }
    
    public String getPath() {
        return path;
    }
    
    @Override
    public boolean matches(OperationDescription desc) {
        boolean ret = path.equals(desc.getPath()) && (range == null || range.includes(desc.getVersion()) );
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
