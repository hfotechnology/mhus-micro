package de.mhus.micro.core.api;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroPublisher {

    void push(OperationDescription desc);
    
    void remove(OperationDescription desc);
    
    void refresh();

}
