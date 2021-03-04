package de.mhus.micro.core.api;

import java.util.List;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroProvider {

    
    void provided(List<OperationDescription> list);

    void reload();
    
}
