package de.mhus.micro.core.impl;

import de.mhus.lib.core.MLog;
import de.mhus.micro.core.api.MicroProtocol;

public abstract class AbstractProtocol extends MLog implements MicroProtocol {

    protected AbstractApi api;

    public void doInit(AbstractApi api) {
        this.api = api;
    }
    
}
