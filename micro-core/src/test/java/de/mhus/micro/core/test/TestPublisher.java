package de.mhus.micro.core.test;

import java.util.ArrayList;
import java.util.UUID;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.impl.AbstractPublisher;

public class TestPublisher extends AbstractPublisher {

	public ArrayList<OperationDescription> list = new ArrayList<>();
	public boolean refresh;
	
	@Override
	public void push(OperationDescription desc) {
		System.out.println("Add " + desc);
		list.add(desc);
	}

	@Override
	public void remove(OperationDescription desc) {
		System.out.println("Remove " + desc);
		UUID uuid = desc.getUuid();
		if (uuid == null) return;
		list.removeIf(v -> uuid.equals(v.getUuid() ) );
	}

	@Override
	public void refresh() {
		this.refresh = true;
	}

}
