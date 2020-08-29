package de.mhus.micro.api.client;

import java.util.List;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroClientDiscoverer {

    void list(MicroFilter filter, List<OperationDescription> results);

}
