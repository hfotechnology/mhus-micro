package de.mhus.micro.core.proto;

import java.util.Enumeration;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.impl.AbstractProtocol;

public class JmsProtocol extends AbstractProtocol {

	private static final String PARAM_OPERATION_PATH = "path";
	private static final String PARAM_OPERATION_VERSION = "version";
	private static final String[] PROTOCOLS = new String[] {"jms"};

	private JmsConnection con = null;
	
	@Override
	public MicroResult execute(OperationDescription desc, INode arguments, IReadProperties properties) {

		try {
	        String queue = desc.getLabels().getString("queue");
	        try (JmsDestination dest = new JmsDestination(queue, false)) {
	    
	            if (con == null) 
	                throw new NotFoundException("jms connection not set");
	            dest.setConnection(con);
	            try (ClientJms client = new ClientJms(dest)) {
	                
	                TextMessage msg = con.createTextMessage();
	                msg.setStringProperty(PARAM_OPERATION_PATH, desc.getPath());
	                msg.setStringProperty(PARAM_OPERATION_VERSION, desc.getVersionString());
	                
	                Subject subject = Aaa.getSubject();
	                if (subject.isAuthenticated()) {
	                    String jwt = Aaa.createBearerToken(subject, null);
	                    if (jwt != null)
	                        msg.setStringProperty("jwt_token", jwt);
	                }
	                String json = INode.toPrettyJsonString(arguments);
	                msg.setText(json);
	                Message res = client.sendJms(msg);
	                
	                MProperties config = new MProperties();
	                for ( @SuppressWarnings("unchecked")
					Enumeration<String> enu = res.getPropertyNames();enu.hasMoreElements();) {
	                	String name = enu.nextElement();
	                	config.put(name, res.getObjectProperty(name));
	                }

	                int rc = res.getIntProperty("rc");
	                String mesg = res.getStringProperty("msg");
	                boolean successful = res.getBooleanProperty("successful");
	                
	                INode result = null;
	                if (res instanceof TextMessage) {
	                    result = INode.readNodeFromString( ((TextMessage)res).getText() );
	                } else
	                if (res instanceof MapMessage) {
	                    result = MJms.getMapConfig((MapMessage)res);
	                }
	    
	                return new MicroResult(successful, rc, mesg, desc, result, config);
	    
	            }
	        }
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
