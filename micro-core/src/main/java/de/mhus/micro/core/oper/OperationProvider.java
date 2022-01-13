package de.mhus.micro.core.oper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.impl.AbstractProvider;

public abstract class OperationProvider extends AbstractProvider implements OperationTracker {

	protected Map<String, OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public void check() {
		
	}

	@Override
	public void reload() {
		
	}

	@Override
	public Boolean discover(Function<OperationDescription, Boolean> action, Function<OperationDescription, Boolean> filter, IReadProperties properties) {
		for ( OperationDescription desc : descriptions.values())
			if (!action.apply(desc) )
				return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	@Override
	public void add(Operation oper) {
		OperationDescription desc = oper.getDescription();
		desc = cloneDescription(oper.getDescription());
		String key = desc.getPathVersion();
		
		descriptions.put(key, desc);
		if (api != null)
			api.updateDescription(desc);
	}
	
	@Override
	public void remove(Operation oper) {
		OperationDescription desc = oper.getDescription();
		String key = desc.getPathVersion();
		
		descriptions.remove(key);
		if (api != null)
			api.removeDescription(desc);
	}

	protected abstract OperationDescription cloneDescription(OperationDescription description);

}
