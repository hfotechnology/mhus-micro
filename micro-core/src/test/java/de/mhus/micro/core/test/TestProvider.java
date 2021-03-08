package de.mhus.micro.core.test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.util.AbstractProvider;

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
	public void discover(Consumer<OperationDescription> action) {
		list.forEach(v -> action.accept(v) );
	}
	
	public void add(OperationDescription desc) {
		list.add(desc);
		api.updateDescription(desc);
	}

	public void remove(OperationDescription desc) {
		list.add(desc);
		api.removeDescription(desc);
	}

}
