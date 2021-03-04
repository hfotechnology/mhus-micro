package de.mhus.micro.core.util;

import de.mhus.lib.core.MLog;
import de.mhus.micro.core.api.MicroDiscovery;

public abstract class AbstractDiscovery extends MLog implements MicroDiscovery {

    protected AbstractApi api;

    public void doInit(AbstractApi api) {
        this.api = api;
    }
    
}
