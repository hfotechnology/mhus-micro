package de.mhus.micro.api.client;

import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroOperation {

    IConfig execute(IConfig arguments);

    OperationDescription getDescription();

}
