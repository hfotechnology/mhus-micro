package de.mhus.micro.core.api;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroProtocol {

	MicroResult execute(OperationDescription desc, INode arguments, IReadProperties properties);

	String[] getNames();

}
