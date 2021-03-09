package de.mhus.micro.core.test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.impl.AbstractProvider;

public class TestProvider extends AbstractProvider {

	public List<OperationDescription> list = new LinkedList<>();
	
	@Override
	public void check() {
		log().i("check");
	}

	@Override
	public void reload() {
		log().i("reload");
	}

	@Override
	public Boolean discover(Function<OperationDescription,Boolean> action) {
		for ( OperationDescription desc : list)
			if (!action.apply(desc) )
				return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	public void add(OperationDescription desc) {
		log().i("add",desc);
		list.add(desc);
		api.updateDescription(desc);
	}

	public void remove(OperationDescription desc) {
		log().i("remove",desc);
		list.add(desc);
		api.removeDescription(desc);
	}

}
