package de.mhus.micro.core.api;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroProtocol {

	MicroResult execute(OperationDescription desc, IConfig arguments, IReadProperties properties);

	String[] getNames();

}
