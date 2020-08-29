package de.mhus.micro.api.client;

import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public class MicroResult {

    private OperationDescription description;
    private IConfig result;

    public MicroResult(OperationDescription description, IConfig result) {
        this.description = description;
        this.result = result;
    }
    public OperationDescription getDescription() {
        return description;
    }
    public IConfig getResult() {
        return result;
    }
    
}
