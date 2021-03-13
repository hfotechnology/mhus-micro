package de.mhus.micro.core.oper;

import de.mhus.lib.core.operation.Operation;

public interface OperationTracker {

	void add(Operation oper);

	void remove(Operation oper);

}
