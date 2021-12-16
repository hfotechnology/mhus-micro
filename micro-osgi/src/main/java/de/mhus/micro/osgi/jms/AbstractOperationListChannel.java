package de.mhus.micro.osgi.jms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;

/**
 * Abstract operation execution but this one can handle a list of operations.
 * add/put and remove operations dynamically to the list or in doActivate()
 * 
 * @see AbstractOperationExecuteChannel
 * @author mikehummel
 *
 */
public abstract class AbstractOperationListChannel extends AbstractOperationExecuteChannel {

	private HashMap<String, Operation> operations = new HashMap<String, Operation>();
	
	@Override
	protected OperationResult doExecute(String path, INode properties) {
		Operation oper = getOperation(path);
		if (oper == null) return new NotSuccessful(path,"not found",OperationResult.NOT_FOUND);
		DefaultTaskContext context = new DefaultTaskContext(this.getClass());
		context.setParameters(properties);
		try {
            if (!oper.hasAccess(context)) {
                return new NotSuccessful(oper, "access denied", -401);
            }
		    if (!oper.canExecute(context)) {
		        return new NotSuccessful(oper, context.getErrorMessage(), -1);
		    }
			return oper.doExecute(context);
		} catch (Throwable t) {
			log().d(path,t);
			return new NotSuccessful(path, t.toString(), OperationResult.INTERNAL_ERROR);
		}
	}

	protected Operation getOperation(String path) {
		synchronized (operations) {
			return operations.get(path);
		}
	}

	@Override
	protected List<String> getOperations() {
		synchronized (operations) {
			LinkedList<String> out = new LinkedList<String>(operations.keySet());
			return out;
		}
	}

	@Override
	protected OperationDescription getOperationDescription(String path) {
		Operation oper = getOperation(path);
		if (oper == null) return null;
		return oper.getDescription();
	}

	protected void add(Operation operation) {
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "de,java,org,com"); //HACK! !!!!
		put(operation.getClass().getCanonicalName(),operation);
	}
	
	protected void put(String path, Operation operation) {
		synchronized (operations) {
			operations.put(path, operation);
		}
	}
	
	protected void remove(String path) {
		synchronized (operations) {
			operations.remove(path);
		}
	}
	
}
