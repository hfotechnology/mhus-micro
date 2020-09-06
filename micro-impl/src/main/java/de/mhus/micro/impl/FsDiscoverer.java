package de.mhus.micro.impl;

import java.util.List;

import org.osgi.framework.BundleContext;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.client.MicroDiscoverer;
import de.mhus.micro.api.client.MicroFilter;

@ServiceComponent
public class FsDiscoverer extends MLog implements MicroDiscoverer {

    @ServiceActivate
    public void doActivate(BundleContext ctx) {
        
    }
    
    @Override
    public void list(MicroFilter filter, List<OperationDescription> results) {
        // TODO Auto-generated method stub
        
    }

}
