package de.mhus.micro.core.api;

import java.util.function.Function;

import de.mhus.lib.core.operation.OperationDescription;

/**
 * Provide external services for local usage.
 * @author mikehummel
 *
 */
public interface MicroDiscovery {

	/**
	 * This is called periodically by the api to update the current state.
	 */
    void check();

    /**
     * This is called to refresh all data.
     */
    void reload();
    
    /**
     * Call action for each known operation.
     * @param action
     * @return true...
     */
	Boolean discover(Function<OperationDescription,Boolean> action);

	/**
	 * Set the priority of the discovery provider to prefer protocols
	 * @return
	 */
    default int getPriority() {
        return 100;
    }
    
}
