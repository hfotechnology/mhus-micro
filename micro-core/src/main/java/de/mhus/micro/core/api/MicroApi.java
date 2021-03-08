package de.mhus.micro.core.api;

import java.util.ArrayList;
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
    default List<MicroResult> execute(MicroFilter filter, IConfig arguments, IProperties properties) throws Exception {
    	List<MicroResult> out = new ArrayList<>();
    	discover(filter, desc -> {
    		try {
    			out.add( execute(desc, arguments, properties) );
    		} catch (Throwable t) {
    			out.add(new MicroResult(desc, t));
    		}
    	} );
    	return out;
    }
    
    /**
     * Execute the operation and returns the result.
     * 
     * @param operation
     * @param arguments
     * @param properties
     * @return
     * @throws Exception
     */
    MicroResult execute(OperationDescription operation, IConfig arguments, IProperties properties) throws Exception;
    
    /**
     * Return all known operation descriptions that matches the filter.
     * 
     * @param filter
     * @param action
     */
    default void discover(MicroFilter filter, Consumer<OperationDescription> action) {
    	discover(desc -> {
    		if (filter.matches(desc))
    			action.accept(desc);
    	});
    }
    
    /**
     * Return all known operation descriptions.
     * 
     * @param filter
     * @param results
     */
    void discover(Consumer<OperationDescription> action);

}
