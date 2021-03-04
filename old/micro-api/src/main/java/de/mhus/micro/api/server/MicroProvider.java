package de.mhus.micro.api.server;

import java.util.List;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroProvider {

    
    void provided(List<OperationDescription> list);

    void reload();
    
}
