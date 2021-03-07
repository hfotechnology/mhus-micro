package de.mhus.micro.core.api;

import java.util.List;
import java.util.function.Consumer;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;

public interface MicroApi {

    /**
     * Execute the found operations and return the results.
     * Depending on the properties this could be one or all or a subset of operations.
     * 
     * @param filter
     * @param arguments
     * @param properties 
     * @return list of results
     * @throws Exception 
     */
    List<MicroResult> execute(MicroFilter filter, IConfig arguments, IProperties properties) throws Exception;
    
    /**
     * Return all operations that match the filter.
     * 
     * @param filter
     * @param results
     */
    void operations(MicroFilter filter, Consumer<MicroOperation> results);

    /**
     * Return all known operation descriptions that match the filter.
     * 
     * @param filter
     * @param results
     */
    void discover(MicroFilter filter, Consumer<OperationDescription> results);

//    List<MicroPusher> getPushers();

    /**
     * Return all known providers.
     * @return Providers
     */
//    List<MicroProvider> getProviders();

//    List<MicroExecutor> getExecutors();

//    List<MicroDiscoverer> getDiscoverer();

}
