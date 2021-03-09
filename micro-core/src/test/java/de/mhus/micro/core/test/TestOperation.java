package de.mhus.micro.core.test;

import de.mhus.lib.annotations.strategy.OperationService;
import de.mhus.lib.core.operation.AbstractOperation;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.Successful;
import de.mhus.lib.core.operation.TaskContext;

@OperationService(title = "Test Operation")
public class TestOperation extends AbstractOperation {

	public boolean done = false;

	@Override
	protected OperationResult doExecute2(TaskContext context) throws Exception {
		done  = true;
		return new Successful(this);
	}

}
