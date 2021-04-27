package de.mhus.micro.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.api.Micro;
import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.api.MicroDiscovery;
import de.mhus.micro.core.api.MicroProtocol;
import de.mhus.micro.core.api.MicroProvider;
import de.mhus.micro.core.api.MicroPublisher;
import de.mhus.micro.core.api.MicroResult;

public /*abstract*/ class AbstractApi extends MLog implements MicroApi {

	private List<MicroDiscovery> discovery = Collections.synchronizedList(new ArrayList<>());
	private List<MicroPublisher> publishers = Collections.synchronizedList(new ArrayList<>());
	private List<MicroProvider> providers = Collections.synchronizedList(new ArrayList<>());
	private Map<String, MicroProtocol> protocols = Collections.synchronizedMap(new HashMap<>());
	
    public void updateDescription(OperationDescription desc) {
        publishers.forEach(p -> { try {
        	p.push(desc);
        } catch (Throwable t) {
        	log().e(desc,p,t);
        } });
    }

    public void removeDescription(OperationDescription desc) {
        publishers.forEach(p -> { try {
        	p.remove(desc);
        } catch (Throwable t) {
        	log().e(desc,p,t);
        } });
    }
    
    public void addProvider(MicroProvider obj) {
    	if (obj instanceof AbstractProvider)
    		((AbstractProvider)obj).doInit(this);
    	discovery.add(obj);
    	providers.add(obj);
    	
    	publishToAll(obj);
    }
    
    public void removeProvider(MicroProvider obj) {
    	discovery.remove(obj);
    	providers.remove(obj);
    	
    	if (obj instanceof AbstractProvider)
    		((AbstractProvider)obj).doDestroy();
    }
    
    
    public void addDiscovery(MicroDiscovery obj) {
    	if (obj instanceof AbstractDiscovery)
    		((AbstractDiscovery)obj).doInit(this);
    	discovery.add(obj);
    	
    }

    public void removeDiscovery(MicroDiscovery obj) {
    	discovery.remove(obj);
    	if (obj instanceof AbstractDiscovery)
    		((AbstractDiscovery)obj).doDestroy();
    	
    }
    
	public void addPublisher(MicroPublisher obj) {
    	if (obj instanceof AbstractPublisher)
    		((AbstractPublisher)obj).doInit(this);
    	publishers.add(obj);
    	publishAll(obj);
    }
	
	public void removePublisher(MicroPublisher obj) {
    	publishers.remove(obj);
    	
    	if (obj instanceof AbstractPublisher)
    		((AbstractPublisher)obj).doDestroy();
    }
	
	public void addProtocol(MicroProtocol obj) {
		for (String proto : obj.getNames())
			addProtocol(proto, obj);
	}

	public void removeProtocol(MicroProtocol obj) {
		for (String proto : obj.getNames())
			removeProtocol(proto);
	}
	
	public void addProtocol(String name, MicroProtocol obj) {
		protocols.put(name, obj);
		if (obj instanceof AbstractProtocol)
			((AbstractProtocol)obj).doInit(this);
	}
	
	public void removeProtocol(String name) {
		MicroProtocol obj = protocols.remove(name);
		if (obj instanceof AbstractProtocol)
			((AbstractProtocol)obj).doDestroy();
	}
	
    private void publishToAll(MicroProvider obj) {
    	obj.discover(
    			desc -> { 
		    		if (!isLocal(desc)) 
		    			publishers.forEach( p -> { try {
		    	        	p.push(desc);
		    	        } catch (Throwable t) {
		    	        	log().e(desc,p,t);
		    	        } } );
		    		return Boolean.TRUE;
    			} );
	}

	private void publishAll(MicroPublisher obj) {
		providers.forEach(v -> v.discover(
				desc -> {
					if (!isLocal(desc))
						obj.push(desc);
					return Boolean.TRUE;
				} ));
	}

	@Override
	public void discover(Function<OperationDescription,Boolean> action) {
		for (MicroDiscovery d : discovery) {
			if (!d.discover(desc -> {
				try {
					return action.apply(desc);
				} catch (Throwable t) {
					log().e(desc,t);
				}
				return Boolean.TRUE;
			})) return;
		}
	}

	private MicroProtocol getProtoExecutor(OperationDescription desc) {
		String proto = desc.getLabels().getString(Micro.LABEL_PROTO, "");
		MicroProtocol executor = protocols.get(proto);
		if (executor == null)
			throw new UnknownProtocolException(proto);
		return executor;
	}

	public boolean isLocal(OperationDescription desc) {
		if (desc == null) return true;
		return desc.getLabels().getBoolean(Micro.LABEL_LOCAL, false);
	}

	public void check() {
		discovery.forEach(d -> d.check() );
	}

	@Override
	public MicroResult execute(OperationDescription desc, INode arguments, IReadProperties properties)
			throws Exception {
		MicroProtocol executor = getProtoExecutor(desc);
		MicroResult res = executor.execute(desc, arguments, properties);
		return res;
	}

	public void forEachProvider(Consumer<MicroProvider> action) {
		providers.forEach(action);
	}

	public void forEachProtocol(Consumer<MicroProtocol> action) {
		new HashSet<MicroProtocol>(protocols.values()).forEach(action);
	}

	public void forEachDiscovery(Consumer<MicroDiscovery> action) {
		discovery.forEach(action);
	}

	public void forEachPublisher(Consumer<MicroPublisher> action) {
		publishers.forEach(action);
	}

}
