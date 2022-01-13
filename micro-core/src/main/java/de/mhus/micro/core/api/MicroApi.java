package de.mhus.micro.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.node.INode;
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
    default List<MicroResult> executeAll(MicroFilter filter, INode arguments, IProperties properties) throws Exception {
    	List<MicroResult> out = new ArrayList<>();
    	discover(filter, desc -> {
    		try {
    			out.add( execute(desc, arguments, properties) );
    		} catch (Throwable t) {
    			out.add(new MicroResult(desc, t));
    		}
    		return Boolean.TRUE;
    	}, properties );
    	return out;
    }
    
    /**
     * Execute the operation and returns the result.
     * 
     * @param description
     * @param arguments
     * @param properties
     * @throws Exception
     * @return The result
     */
    MicroResult execute(OperationDescription description, INode arguments, IReadProperties properties) throws Exception;
    
    default MicroResult execute(String pathVersion, INode arguments, IReadProperties properties) throws Exception {
    	OperationDescription description = first(new FilterPathVersion(pathVersion), properties);
    	if (description == null) throw new NotFoundException("@Operation for path $1 not found",pathVersion);
    	return execute(description, arguments, properties);
    }
    
    default MicroResult execute2(String pathVersion, INode arguments, Object ... propertiesKV) throws Exception {
        MProperties properties = null;
        if (propertiesKV != null)
            properties = IProperties.to(propertiesKV);
        OperationDescription description = first(new FilterPathVersion(pathVersion), properties);
        if (description == null) throw new NotFoundException("@Operation for path $1 not found",pathVersion);
        return execute(description, arguments, properties);
    }
    
    default OperationDescription first(MicroFilter filter, IReadProperties properties) {
    	Value<OperationDescription> first = new Value<>();
    	discover(desc -> {
    		if (filter.matches(desc)) {
    			first.value = desc;
    			return Boolean.FALSE;
    		}
    		return Boolean.TRUE;
    	}, properties);
    	return first.value;
    }
    
    /**
     * Return all known operation descriptions that matches the filter.
     * 
     * @param filter
     * @param action
     * @param properties 
     */
    default void discover(MicroFilter filter, Function<OperationDescription,Boolean> action, IReadProperties properties) {
    	discover(desc -> {
    		if (filter.matches(desc))
    			return action.apply(desc);
    		return Boolean.TRUE;
    	}, properties);
    }
    
    /**
     * Return all known operation descriptions.
     * @param action 
     * @param properties 
     * @param filter
     * @param results
     */
    void discover(Function<OperationDescription,Boolean> action, IReadProperties properties);

}
