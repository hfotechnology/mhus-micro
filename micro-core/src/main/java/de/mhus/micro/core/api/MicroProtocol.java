package de.mhus.micro.core.api;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroProtocol {

	MicroResult execute(OperationDescription desc, IConfig arguments, IProperties properties);

	String[] getNames();

}
