package de.mhus.micro.oper.rest;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.errors.MException;
import de.mhus.rest.core.CallContext;

public class RestTaskContext extends  DefaultTaskContext {

    public RestTaskContext(CallContext context, String load) throws MException {
        super(OperationsNode.class);
        IConfig params = IConfig.readConfigFromString(load);
        setParameters(params);
        setConfig(MApi.getCfg(OperationsNode.class));
        setTestOnly(false); //TODO
    }

}
