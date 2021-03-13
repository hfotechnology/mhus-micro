package de.mhus.micro.core.impl.services;

import de.mhus.lib.annotations.activator.DefaultImplementation;
import de.mhus.micro.core.api.MicroApi;

@DefaultImplementation(DefaultMicroApiServiceProvider.class)
public interface MicroApiServiceProvider {

	MicroApi getApi();
	
}
