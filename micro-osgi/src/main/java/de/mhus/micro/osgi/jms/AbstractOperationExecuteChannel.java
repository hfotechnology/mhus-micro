package de.mhus.micro.osgi.jms;

import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.MNode;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.Successful;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.micro.jms.MicroJmsUtil;
import de.mhus.osgi.api.jms.AbstractJmsDataChannel;

/**
 * The class implement the protocol to provide a 'operation' connection via JMS. It's the
 * backend of operation connectors e.g. in Bonitasoft or the admin interface.
 * 
 * For a sample implementation see de.hfo.magic.mws.core.impl.operation.OperationExecuteChannel
 * 
 * @author mikehummel
 *
 */
public abstract class AbstractOperationExecuteChannel extends AbstractJmsDataChannel {

	@Override
    protected JmsChannel createChannel() throws JMSException {
		return new ServerJms(new JmsDestination(getQueueName(), false)) {

			@Override
			public void receivedOneWay(Message msg) throws JMSException {
				received(msg);
			}

			@Override
			public Message received(Message msg) throws JMSException {
				return AbstractOperationExecuteChannel.this.received(msg);
			}
			
		};
	}

	public AbstractOperationExecuteChannel() {
		connectionName = getJmsConnectionName();
	}
		
	protected ServerJms getServer() {
		return (ServerJms) getChannel();
	};
	
	@SuppressWarnings({ "deprecation" })
	protected Message received(Message msg) throws JMSException {
		
		log().d("Operation Call",msg);
		
		String path = msg.getStringProperty(MicroJmsUtil.PARAM_OPERATION_PATH);
		if (path == null) return null;
		
        if (msg instanceof BytesMessage) 
            log().w("Received byte message",msg);
        else
        if (msg instanceof ObjectMessage) {
            log().w("Received serialized object message",msg);
            throw new NotSupportedException("Received object message");
        }
		
        INode properties = MJms.toNode(msg);
        
//		INode properties = null;
//		if (msg instanceof MapMessage) {
//			properties = MJms.getMapConfig((MapMessage)msg);
//		} else
//		if (msg instanceof ObjectMessage) {
//			Serializable obj = ((ObjectMessage)msg).getObject();
//			if (obj == null) {
//				
//			} else
//			if (obj instanceof MProperties) {
//				properties = INode.readFromProperties( (MProperties)obj );
//			} else
//			if (obj instanceof Map) {
//				properties = INode.readFromMap( (Map)obj );
//			}
//		}
		
		if (properties == null)
			properties = new MNode(); // empty
		
		OperationResult res = null;
		if (path.equals(MicroJmsUtil.OPERATION_LIST)) {
			String list = MString.join(getOperations().iterator(), ",");
			res = new Successful(MicroJmsUtil.OPERATION_LIST, "list",OperationResult.OK,"list",list);
		} else
		if (path.equals(MicroJmsUtil.OPERATION_INFO)) {
			String id = properties.getString(MicroJmsUtil.PARAM_OPERATION_ID, null);
			if (id == null) 
				res = new NotSuccessful(MicroJmsUtil.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
			else {
				OperationDescription des = getOperationDescription(id);
				if (des == null)
					res = new NotSuccessful(MicroJmsUtil.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
				else {
					res = new Successful(MicroJmsUtil.OPERATION_INFO, "list",OperationResult.OK,
					        "path", des.getPath(),
					        "version", des.getVersionString(),
							"form",String.valueOf( des.getForm() ),
							"title",des.getTitle()
							);
				}
			}
		} else
			res = doExecute(path, properties);
		
		Message ret = null;
		if (res != null) {
		    ret = MJms.toMessage(getServer(), res.getResult());
            ret.setIntProperty("rc", res.getReturnCode());
            ret.setStringProperty("msg", res.getMsg());
            ret.setBooleanProperty("successful", res.isSuccessful());
            OperationDescription next = res.getNextOperation();
            if (next != null) {
                ret.setStringProperty("next.path", next.getPath());
            }
            ret.setStringProperty("path", path);
		} else {
		    ret = getServer().createTextMessage();
			ret.setIntProperty("rc", OperationResult.INTERNAL_ERROR);
			ret.setStringProperty("msg", "null");
			ret.setBooleanProperty("successful", false);
	        ret.setStringProperty("path", path);
		}
		prepareResultMessage(msg,ret);
		// ret.setStringProperty("host", MSystem.getHostname());
		// ret.setStringProperty("user", MSystem.getSystemUser());
		return ret;
	}
	
	protected abstract void prepareResultMessage(Message request, Message result);

    protected String getServiceName() {
		return getClass().getCanonicalName();
	}

	/**
	 * Return the name of the queue the service is reachable in the JMS universe.
	 * The method is called once starting the service.
	 * @return The name of the queue - must be unique in the JMS universe.
	 */
	protected abstract String getQueueName();
	
	/**
	 * Return the name of the jms connection which the service should be bound.
	 * The method is called once starting the service.
	 * @return The name of the connection, configured in the framework (jms:connection-list).
	 */
	protected abstract String getJmsConnectionName();

	/**
	 * This event is triggered if the operation is called. Execute the command and return a
	 * result to inform the caller.
	 * 
	 * @param path The name of the command
	 * @param properties properties given.
	 * @return
	 */
	protected abstract OperationResult doExecute(String path, INode properties);

	/**
	 * Return a list of current possible and public operations. The list can be called
	 * periodically and return another set of operation paths depending on the
	 * current situation.
	 * 
	 * It's a lightweight information about the operations. You only need 
	 * to implement it if you wan't to allow introspection of the channel.
	 * 
	 * @return A list of operation paths.
	 */
	protected abstract List<String> getOperations();
	
	/**
	 * Return the description of the operation with the specified path.
	 * It's a lightweight information about the operations. You only need 
	 * to implement it if you wan't to allow introspection of the channel.
	 * 
	 * @param path The path to the operation
	 * @return The description or null.
	 */
	protected abstract OperationDescription getOperationDescription(String path);
	
}
