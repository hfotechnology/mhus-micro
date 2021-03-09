package de.mhus.micro.core.oper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.util.AbstractProtocol;

public class LocalOperationProtocol extends AbstractProtocol {

	private static final String[] PROTOCOL = new String[] {"operation"};
	private Map<String,Operation> operations = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public MicroResult execute(OperationDescription desc, IConfig arguments, IProperties properties) {

		String key = desc.getPathVersion();
		Operation oper = operations.get(key);
		
		DefaultTaskContext context = new DefaultTaskContext(oper.getClass());
		context.setParameters(arguments);

		try {
			OperationResult res = oper.doExecute(context);
			return new MicroResult(res.isSuccessful(), res.getReturnCode(), res.getMsg(), desc, res.getResultAsConfig());
		} catch (Throwable e) {
			return new MicroResult(desc, e);
		}
		
	}

	@Override
	public String[] getNames() {
		return PROTOCOL;
	}

	public void add(Operation oper) {
		OperationDescription desc = oper.getDescription();
		String key = desc.getPathVersion();
		
		operations.put(key, oper);
	}

	public void remove(Operation oper) {
		OperationDescription desc = oper.getDescription();
		String key = desc.getPathVersion();
		
		operations.remove(key);
	}

}
