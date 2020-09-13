package de.mhus.micro.impl;

import java.util.Collection;
import java.util.Map;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.TaskContext;
import de.mhus.lib.errors.MException;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.client.MicroResult;

public class LocalOperation implements MicroOperation {

    private OperationDescription desc;
    private Operation oper;

    public LocalOperation(OperationDescription desc, Operation operation) {
        this.desc = desc;
        this.oper = operation;
    }

    @Override
    public OperationDescription getDescription() {
        return desc;
    }

    @Override
    public MicroResult execute(IConfig arguments, IProperties properties) throws Exception {
        TaskContext taskContext = new LocalTaskContext(arguments);
        
        if (!oper.hasAccess(taskContext))
            throw new MException("not found (2)",taskContext.getErrorMessage());
        
        if (!oper.canExecute(taskContext))
            throw new MException("not found (3)",taskContext.getErrorMessage());
            
        OperationResult res = oper.doExecute(taskContext);
        
        Object r = res.getResult();
        if (r != null) {
            // map result to rest result
            if (r instanceof IConfig) {
                return new MicroResult(true, 0, "",getDescription(), (IConfig)r);
            }
            if (r instanceof Map) {
                return new MicroResult(true, 0, "",getDescription(), IConfig.readFromMap((Map<?,?>)r));
            }
            if (r instanceof Collection<?>) {
                return new MicroResult(true, 0, "",getDescription(), IConfig.readFromCollection((Collection<?>)r));
            }
            throw new MException("return type not supported");
        }
        return null;
    }

}
