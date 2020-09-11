package de.mhus.micro.impl;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.DefaultTaskContext;

public class LocalTaskContext extends DefaultTaskContext {

    public LocalTaskContext(IConfig arguments) {
        super(LocalOperationsLoopback.class);
        setParameters(arguments);
        setConfig(MApi.getCfg(LocalOperationsLoopback.class));
        setTestOnly(false); //TODO
    }



}
