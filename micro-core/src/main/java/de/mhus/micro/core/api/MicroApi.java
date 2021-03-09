package de.mhus.micro.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Value;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.core.filter.FilterPathVersion;

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
    default List<MicroResult> executeAll(MicroFilter filter, IConfig arguments, IProperties properties) throws Exception {
    	List<MicroResult> out = new ArrayList<>();
    	discover(filter, desc -> {
    		try {
    			out.add( execute(desc, arguments, properties) );
    		} catch (Throwable t) {
    			out.add(new MicroResult(desc, t));
    		}
    		return Boolean.TRUE;
    	} );
    	return out;
    }
    
    /**
     * Execute the operation and returns the result.
     * 
     * @param description
     * @param arguments
     * @param properties
     * @return
     * @throws Exception
     */
    MicroResult execute(OperationDescription description, IConfig arguments, IProperties properties) throws Exception;
    
    default MicroResult execute(String pathVersion, IConfig arguments, IProperties properties) throws Exception {
    	OperationDescription description = first(new FilterPathVersion(pathVersion));
    	if (description == null) throw new NotFoundException("@Operation for path $1 not found",pathVersion);
    	return execute(description, arguments, properties);
    }
    
    default OperationDescription first(MicroFilter filter) {
    	Value<OperationDescription> first = new Value<>();
    	discover(desc -> {
    		if (filter.matches(desc)) {
    			first.value = desc;
    			return Boolean.FALSE;
    		}
    		return Boolean.TRUE;
    	});
    	return first.value;
    }
    
    /**
     * Return all known operation descriptions that matches the filter.
     * 
     * @param filter
     * @param action
     */
    default void discover(MicroFilter filter, Function<OperationDescription,Boolean> action) {
    	discover(desc -> {
    		if (filter.matches(desc))
    			return action.apply(desc);
    		return Boolean.TRUE;
    	});
    }
    
    /**
     * Return all known operation descriptions.
     * 
     * @param filter
     * @param results
     */
    void discover(Function<OperationDescription,Boolean> action);

}
