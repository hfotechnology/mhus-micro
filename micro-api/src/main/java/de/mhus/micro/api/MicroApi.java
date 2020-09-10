package de.mhus.micro.api;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.client.MicroResult;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.micro.api.server.MicroPusher;

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
    void operations(MicroFilter filter, List<MicroOperation> results);

    /**
     * Return all known operation descriptions that match the filter.
     * 
     * @param filter
     * @param results
     */
    void discover(MicroFilter filter, List<OperationDescription> results);

    List<MicroPusher> getPushers();

    /**
     * Return all known providers.
     * @return Providers
     */
    List<MicroProvider> getProviders();

}
