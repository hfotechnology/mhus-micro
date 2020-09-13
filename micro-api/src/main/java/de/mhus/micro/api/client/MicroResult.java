package de.mhus.micro.api.client;

import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public class MicroResult {

    public static final int OK = 0;
    
    private OperationDescription description;
    private IConfig result;
    private int rc;
    private String msg;

    private boolean transportSuccessful;

    public MicroResult(boolean transportSuccessful, int rc, String msg, OperationDescription description, IConfig result) {
        this.transportSuccessful = transportSuccessful;
        this.rc = rc;
        this.msg = msg;
        this.description = description;
        this.result = result;
    }
    public OperationDescription getDescription() {
        return description;
    }
    public IConfig getResult() {
        return result;
    }
    public int getReturnCode() {
        return rc;
    }

    public String getMessage() {
        return msg;
    }
    public boolean isTransportSuccessful() {
        return transportSuccessful;
    }
    
    @Override
    public String toString() {
        return transportSuccessful + "," + rc + "," + msg + "," + (description != null ? description.getPath() : "?") + "," + result;
    }
}
