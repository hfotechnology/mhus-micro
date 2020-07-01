package de.mhus.micro.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.MicroClientProvider;
import de.mhus.osgi.api.util.MServiceTracker;

@Component
public class MicroApiImpl extends MLog implements MicroApi {

    private MServiceTracker<MicroClientProvider> tracker = null;
    private List<MicroClientProvider> providers = Collections.synchronizedList(new LinkedList<MicroClientProvider>());
    
    @Activate
    public void doActivate(BundleContext ctx) {
        tracker = new MServiceTracker<MicroClientProvider>(ctx, MicroClientProvider.class) {

            @Override
            protected void removeService(ServiceReference<MicroClientProvider> reference, MicroClientProvider service) {
                providers.remove(service);
            }

            @Override
            protected void addService(ServiceReference<MicroClientProvider> reference, MicroClientProvider service) {
                providers.add(service);
            }
        };
        tracker.start();
    }
    
    @Deactivate
    public void doDeactivate() {
        if (tracker != null)
            tracker.stop();
        tracker = null;
    }

    @Override
    public IConfig execute(String path, IConfig arguments) {
        for (MicroClientProvider p : providers) {
            IConfig res = p.execute(path, arguments);
            if (res != null) return res;
        }
        return null;
    }

    @Override
    public void list(String filter, List<OperationDescription> results) {
        providers.forEach(p -> p.list(filter, results) );
    }

}
