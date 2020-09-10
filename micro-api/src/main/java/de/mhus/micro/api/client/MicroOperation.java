package de.mhus.micro.api.client;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroOperation {

    IConfig execute(IConfig arguments, IProperties properties) throws Exception;

    OperationDescription getDescription();

}
