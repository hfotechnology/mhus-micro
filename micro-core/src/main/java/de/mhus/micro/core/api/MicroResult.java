package de.mhus.micro.core.api;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;

public class MicroResult {

    public static final int OK = 0;
    
    private OperationDescription description;
    private INode result;
    private int rc;
    private String msg;
    private boolean transportSuccessful;
	private Throwable error;
	private IReadProperties properties;

    public MicroResult(boolean transportSuccessful, int rc, String msg, OperationDescription description, INode result, IReadProperties properties) {
        this.transportSuccessful = transportSuccessful;
        this.rc = rc;
        this.msg = msg;
        this.description = description;
        this.result = result;
        this.properties = properties;
    }
    public MicroResult(OperationDescription desc, Throwable t) {
        this.transportSuccessful = false;
        this.rc = -500;
        this.msg = t.toString();
        this.description = desc;
        this.result = null;
        this.error = t;
	}
    
	public OperationDescription getDescription() {
        return description;
    }
    public INode getResult() {
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
    public Throwable getError() {
    	return error;
    }
    public IReadProperties getProperties() {
    	return properties;
    }
    
    @Override
    public String toString() {
        return transportSuccessful + "," + rc + "," + msg + "," + (description != null ? description.getPath() : "?") + "," + result;
    }
}
