package de.mhus.micro.api;

import java.util.List;

import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroApi {

    IConfig execute(String path, IConfig arguments);
    
    void list(String filter, List<OperationDescription> results);
    
}
