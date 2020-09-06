package de.mhus.micro.impl;

import org.osgi.framework.BundleContext;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MLog;
import de.mhus.micro.api.server.MicroPusher;

@ServiceComponent
public class FsPusher extends MLog implements MicroPusher {

    @ServiceActivate
    public void doActivate(BundleContext ctx) {
        
    }
}
