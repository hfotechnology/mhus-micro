package de.mhus.micro.jms;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.micro.core.api.Micro;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.impl.AbstractProtocol;

public class JmsProtocol extends AbstractProtocol {

	private static final String[] PROTOCOLS = new String[] {"jms"};

	private JmsConnection con = null;
	
	@Override
	public MicroResult execute(OperationDescription desc, INode arguments, IReadProperties properties) {

	    
		try {
		    if (con == null) 
		        throw new NotFoundException("jms connection not set");
		    
	        String queue = desc.getLabels().getString(MicroJmsUtil.LABEL_QUEUE, null);
	        long timeout = MicroJmsUtil.CFG_DEFAULT_JMS_TIMEOUT.value();
	        if (properties != null)
	            timeout = properties.getLong(MicroJmsUtil.TIMEOUT, timeout);
	        boolean needAnswer = properties == null || properties.getBoolean(Micro.NEED_ANSWER, true);
	        boolean forceMapMessage = properties != null && properties.getBoolean(MicroJmsUtil.FORCE_MAP_MESSAGE, false);
	        OperationResult res = MicroJmsUtil.doExecuteOperation(con,queue,desc.getPath(),desc.getVersionString(),arguments,timeout,needAnswer,forceMapMessage);

            return new MicroResult(res.isSuccessful(), res.getReturnCode(), res.getMsg(), desc, res.getResultAsNode(), null);
		} catch (Throwable t) {
			return new MicroResult(desc, t);
		}
	        
	}

	@Override
	public String[] getNames() {
		return PROTOCOLS;
	}

	public JmsConnection getConnection() {
		return con;
	}

	public void setConnection(JmsConnection con) {
		this.con = con;
	}

}
