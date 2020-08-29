package de.mhus.micro.impl;

import java.util.ArrayList;
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
import de.mhus.micro.api.client.MicroApi;
import de.mhus.micro.api.client.MicroClientDiscoverer;
import de.mhus.micro.api.client.MicroClientExecutor;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.client.MicroResult;
import de.mhus.osgi.api.util.MServiceTracker;

@Component
public class MicroApiImpl extends MLog implements MicroApi {

    private MServiceTracker<MicroClientExecutor> executorTracker = null;
    private List<MicroClientExecutor> executors = Collections.synchronizedList(new LinkedList<MicroClientExecutor>());
    
    private MServiceTracker<MicroClientDiscoverer> discoverTracker = null;
    private List<MicroClientDiscoverer> discoverers = Collections.synchronizedList(new LinkedList<MicroClientDiscoverer>());
    
    @Activate
    public void doActivate(BundleContext ctx) {
        executorTracker = new MServiceTracker<MicroClientExecutor>(ctx, MicroClientExecutor.class) {

            @Override
            protected void removeService(ServiceReference<MicroClientExecutor> reference, MicroClientExecutor service) {
                executors.remove(service);
            }

            @Override
            protected void addService(ServiceReference<MicroClientExecutor> reference, MicroClientExecutor service) {
                executors.add(service);
            }
        };
        executorTracker.start();
        
        discoverTracker = new MServiceTracker<MicroClientDiscoverer>(ctx, MicroClientDiscoverer.class) {

            @Override
            protected void removeService(ServiceReference<MicroClientDiscoverer> reference, MicroClientDiscoverer service) {
                discoverers.remove(service);
            }

            @Override
            protected void addService(ServiceReference<MicroClientDiscoverer> reference, MicroClientDiscoverer service) {
                discoverers.add(service);
            }
        };
        discoverTracker.start();
        
    }
    
    @Deactivate
    public void doDeactivate() {
        if (executorTracker != null)
            executorTracker.stop();
        executorTracker = null;
        if (discoverTracker != null)
            discoverTracker.stop();
        discoverTracker = null;
    }

    @Override
    public MicroResult execute(MicroFilter filter, IConfig arguments) {
        ArrayList<MicroOperation> list = new ArrayList<>();
        list(filter, list);
        for (MicroOperation oper : list) {
            IConfig res = oper.execute(arguments);
            if (res != null)
                return new MicroResult(oper.getDescription(), res);
        }
        return null;
    }

    @Override
    public List<MicroResult> executeAll(MicroFilter filter, IConfig arguments) {
        ArrayList<MicroOperation> list = new ArrayList<>();
        ArrayList<MicroResult> res = new ArrayList<>();
        list(filter, list);
        for (MicroOperation oper : list) {
            IConfig r = oper.execute(arguments);
            if (r != null)
                res.add(new MicroResult(oper.getDescription(), r));
        }
        return res;
    }
    
    @Override
    public void list(MicroFilter filter, List<MicroOperation> results) {
        executors.forEach(p -> p.list(filter, results) );
    }

    @Override
    public void discover(MicroFilter filter, List<MicroOperation> results) {
        
    }

}
