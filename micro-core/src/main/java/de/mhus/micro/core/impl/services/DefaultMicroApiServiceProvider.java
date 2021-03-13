package de.mhus.micro.core.impl.services;

import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.impl.AbstractApi;

public class DefaultMicroApiServiceProvider implements MicroApiServiceProvider {

	private AbstractApi api;

	@Override
	public synchronized MicroApi getApi() {
		if (api == null)
			api = new AbstractApi();
		return api;
	}

}
