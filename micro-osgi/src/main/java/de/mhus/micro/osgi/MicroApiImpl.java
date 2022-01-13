package de.mhus.micro.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.M;
import de.mhus.lib.core.operation.Operation;
import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;
import de.mhus.micro.core.impl.services.MicroDiscoveryServiceProvider;
import de.mhus.micro.core.impl.services.MicroProtocolServiceProvider;
import de.mhus.micro.core.impl.services.MicroProviderServiceProvider;
import de.mhus.micro.core.impl.services.MicroPublisherServiceProvider;
import de.mhus.micro.core.oper.LocalOperationProtocol;
import de.mhus.micro.core.oper.LocalOperationProvider;
import de.mhus.micro.core.oper.OperationTracker;
import de.mhus.osgi.api.services.OperationManager;
import de.mhus.osgi.api.util.MServiceTracker;

@Component(immediate = true, service = MicroApiServiceProvider.class)
public class MicroApiImpl implements MicroApiServiceProvider {

	private OsgiMicroApi api = new OsgiMicroApi();
	MServiceTracker<MicroDiscoveryServiceProvider> discoveryTracker = 
			new MServiceTracker<>(
					MicroDiscoveryServiceProvider.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);
	MServiceTracker<MicroPublisherServiceProvider> publisherTracker = 
			new MServiceTracker<>(
					MicroPublisherServiceProvider.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);
	MServiceTracker<MicroProviderServiceProvider> providerTracker = 
			new MServiceTracker<>(
					MicroProviderServiceProvider.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);
	MServiceTracker<MicroProtocolServiceProvider> protocolTracker = 
			new MServiceTracker<>(
					MicroProtocolServiceProvider.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);
	MServiceTracker<Operation> operationTracker = 
			new MServiceTracker<>(
					Operation.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);
	
	private LocalOperationProtocol operProtocol;
	private LocalOperationProvider operProvider;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		discoveryTracker.start(ctx);
		publisherTracker.start(ctx);
		providerTracker.start(ctx);
		protocolTracker.start(ctx);
		
		operProtocol = new LocalOperationProtocol() {
			@Override
            protected Operation findOperation(String key) {
				return M.l(OperationManager.class).getOperation(key);
			}
		};
		api.addProtocol(operProtocol);
		
		operProvider = new LocalOperationProvider();
		api.addProvider(operProvider);
		
		operationTracker.start(ctx);
	}

	@Deactivate
	public void doDeactivate() {
		operationTracker.stop();
		discoveryTracker.stop();
		publisherTracker.stop();
		providerTracker.stop();
		protocolTracker.stop();
	}
	
	private void remove(ServiceReference<MicroProtocolServiceProvider> reference,
			MicroProtocolServiceProvider service) {
		api.removeProtocol(service.getService());
	}

	private void add(ServiceReference<MicroProtocolServiceProvider> reference, MicroProtocolServiceProvider service) {
		api.addProtocol(service.getService());
	}

	private void remove(ServiceReference<MicroProviderServiceProvider> reference,
			MicroProviderServiceProvider service) {
		api.removeProvider(service.getService());
	}

	private void add(ServiceReference<MicroProviderServiceProvider> reference, MicroProviderServiceProvider service) {
		api.addProvider(service.getService());
	}

	private void remove(ServiceReference<MicroPublisherServiceProvider> reference,
			MicroPublisherServiceProvider service) {
		api.removePublisher(service.getService());
	}

	private void add(ServiceReference<MicroPublisherServiceProvider> reference,
			MicroPublisherServiceProvider service) {
		api.addPublisher(service.getService());
	}

	private void remove(ServiceReference<MicroDiscoveryServiceProvider> reference,
			MicroDiscoveryServiceProvider service) {
		api.removeDiscovery(service.getService());
	}

	private void add(ServiceReference<MicroDiscoveryServiceProvider> reference,
			MicroDiscoveryServiceProvider service) {
		api.addDiscovery(service.getService());
	}

	private void remove(ServiceReference<Operation> reference, Operation service) {
		api.forEachProvider(p -> {
			if (p instanceof OperationTracker)
				((OperationTracker)p).remove(service);
		});
	}

	private void add(ServiceReference<Operation> reference, Operation service) {
		api.forEachProvider(p -> {
			if (p instanceof OperationTracker)
				((OperationTracker)p).add(service);
		});
	}

	@Override
	public MicroApi getApi() {
		return api;
	}

}
