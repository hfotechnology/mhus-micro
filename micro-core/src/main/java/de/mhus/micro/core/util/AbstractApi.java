package de.mhus.micro.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.api.MicroDiscovery;
import de.mhus.micro.core.api.MicroFilter;
import de.mhus.micro.core.api.MicroOperation;
import de.mhus.micro.core.api.MicroPublisher;
import de.mhus.micro.core.api.MicroResult;

public abstract class AbstractApi extends MLog implements MicroApi {

	private List<MicroDiscovery> discovery = Collections.synchronizedList(new ArrayList<>());
	private List<MicroPublisher> publishers = Collections.synchronizedList(new ArrayList<>());
	
	
    public void updateDescription(OperationDescription desc) {
        // TODO Auto-generated method stub
        
    }
    
    public void addDiscovery(MicroDiscovery obj) {
    	if (obj instanceof AbstractDiscovery)
    		((AbstractDiscovery)obj).doInit(this);
    	discovery.add(obj);
    	
    	publishToAll(obj);
    }

	public void addPublisher(MicroPublisher obj) {
    	if (obj instanceof AbstractPublisher)
    		((AbstractPublisher)obj).doInit(this);
    	publishers.add(obj);
    	publishAll(obj);
    }

    private void publishToAll(MicroDiscovery obj) {
    	obj.discover(MicroFilter.ALL, desc -> publishers.forEach( p -> p.push(desc) ));
	}

	private void publishAll(MicroPublisher obj) {
		discovery.forEach(v -> v.discover(MicroFilter.ALL, f -> obj.push(f) ));
	}


	@Override
	public List<MicroResult> execute(MicroFilter filter, IConfig arguments, IProperties properties) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void operations(MicroFilter filter, Consumer<MicroOperation> results) {
		
	}

	@Override
	public void discover(MicroFilter filter, Consumer<OperationDescription> results) {
		// TODO Auto-generated method stub
		
	}

}
