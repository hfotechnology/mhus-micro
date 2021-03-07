package de.mhus.micro.core.api;

import java.util.function.Consumer;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroDiscovery {

    void check();

    void reload();
    
	void discover(MicroFilter filter, Consumer<OperationDescription> action);
    
}
