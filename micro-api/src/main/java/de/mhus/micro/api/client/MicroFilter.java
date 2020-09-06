package de.mhus.micro.api.client;

import de.mhus.lib.core.operation.OperationDescription;

public class MicroFilter {

    protected String path;

    public MicroFilter(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean matches(OperationDescription desc) {
        return path.equals(desc.getPath());
    }
    
}
