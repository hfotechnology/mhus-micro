package de.mhus.micro.core.oper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.impl.AbstractProtocol;

public class LocalOperationProtocol extends AbstractProtocol implements OperationTracker {

	private static final String[] PROTOCOL = new String[] {"operation"};
	private Map<String,Operation> operations = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public MicroResult execute(OperationDescription desc, INode arguments, IReadProperties properties) {

		String key = desc.getPathVersion();
		Operation oper = findOperation(key);
		
		DefaultTaskContext context = new DefaultTaskContext(oper.getClass());
		context.setParameters(arguments);

		try {
			OperationResult res = oper.doExecute(context);
			return new MicroResult(res.isSuccessful(), res.getReturnCode(), res.getMsg(), desc, res.getResultAsNode(), properties);
		} catch (Throwable e) {
			log().d(desc,e);
			return new MicroResult(desc, e);
		}
		
	}

	protected Operation findOperation(String key) {
		return  operations.get(key);
	}

	@Override
	public String[] getNames() {
		return PROTOCOL;
	}

	@Override
	public void add(Operation oper) {
		OperationDescription desc = oper.getDescription();
		String key = desc.getPathVersion();
		
		operations.put(key, oper);
	}

	@Override
	public void remove(Operation oper) {
		OperationDescription desc = oper.getDescription();
		String key = desc.getPathVersion();
		
		operations.remove(key);
	}

}
