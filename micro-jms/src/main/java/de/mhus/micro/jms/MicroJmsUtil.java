package de.mhus.micro.jms;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.MutableOperationResult;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.core.api.Micro;


public class MicroJmsUtil {


    private static Log log = Log.getLog(MicroJmsUtil.class);
    
    public static final String LOG_MAPPER = "_mhus_log";
    public static CfgLong CFG_DEFAULT_JMS_TIMEOUT = new CfgLong(MicroJmsUtil.class,"defaultJmsTimeout",  MPeriod.MINUTE_IN_MILLISECOUNDS * 2);
    public static final String PARAM_OPERATION_PATH = "path";
    public static final String OPERATION_LIST = "_list";
    public static final String OPERATION_INFO = "_get";
    public static final String PARAM_SUCCESSFUL = "successful";
    public static final String PARAM_MSG  = "msg";
    public static final String PARAM_RC = "rc";
    public static final String PARAM_OPERATION_ID = "id";

    public static final String FORCE_MAP_MESSAGE = "forceMapMessage";

    public static final String TIMEOUT = "timeout";

    private static final String PARAM_OPERATION_VERSION = "version";

    public static final String LABEL_QUEUE = "@queue";

    /**
     * Execute an operation via JMS.
     * 
     * @param con The JMS Connection to use
     * @param queueName Name of the operation provider
     * @param operationName path of the operation
     * @param version 
     * @param parameters parameters to send
     * @param needAnswer If you only need to execute the operation set to false
     * @return The answer of the operation or null if no answer is needed
     * @throws Exception
     */
    public static OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, String version, IProperties parameters, boolean needAnswer ) throws Exception {
//      String ticket = user == null ? Aaa.createTrustTicket(operationName, Aaa.ADMIN) TicketUtil.createAdminTrustTicket() : TicketUtil.createTrustTicket(user);
        return doExecuteOperation(con, queueName, operationName, version, parameters, CFG_DEFAULT_JMS_TIMEOUT.value(), needAnswer);
    }
    
    public static OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, String version, IProperties parameters, boolean needAnswer, long timeout ) throws Exception {
//      String ticket = user == null ? TicketUtil.createAdminTrustTicket() : TicketUtil.createTrustTicket(user);
        return doExecuteOperation(con, queueName, operationName, version, parameters, timeout, needAnswer, false);
    }
    
    public static OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, String version, IProperties parameters, long timeout, boolean needAnswer ) throws Exception {
        return doExecuteOperation(con, queueName, operationName, version, parameters, timeout, needAnswer, false);
    }
    
    @SuppressWarnings("deprecation")
    public static OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, String version, IProperties parameters, long timeout, boolean needAnswer, boolean forceMapMessage ) throws Exception {

        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "de,java,org,com"); //HACK! !!!!

        if (con == null) throw new JMSException("connection is null");
        try (ClientJms client = new ClientJms(con.createQueue(queueName))) {

            Message msg = null;
            if (forceMapMessage) {
                msg = con.createMapMessage();
                for (Entry<String, Object> item : parameters) {
                    @SuppressWarnings("unused")
                    String name = item.getKey();
                    //if (!name.startsWith("_"))
                    Object value = item.getValue();
                    if (value != null && value instanceof Date) 
                        value = MDate.toIsoDateTime((Date)value);
                    else
                    if (value != null && !(value instanceof String) && !value.getClass().isPrimitive() ) value = String.valueOf(value);
                    ((MapMessage)msg).setObject(item.getKey(), value); //TODO different types, currently it's only String ?!
                }
                ((MapMessage)msg).getMapNames();
            } else
                msg = MJms.toMessage(con, parameters);
            
            msg.setStringProperty(PARAM_OPERATION_PATH, operationName);
            msg.setStringProperty(PARAM_OPERATION_VERSION, version);
            
            client.setTimeout(timeout);
            // Send Request
            
            log.d(operationName,"sending Message", queueName, msg, needAnswer, timeout);
            
            if (!needAnswer) {
                client.sendJmsOneWay(msg);
                return null;
            }
            
            Message answer = client.sendJms(msg);
    
            // Process Answer
            
            MutableOperationResult out = new MutableOperationResult();
            out.setOperationPath(operationName);
            if (answer == null) {
                log.d(queueName,operationName,"answer is null");
                out.setSuccessful(false);
                out.setMsg("answer is null");
                out.setReturnCode(OperationResult.INTERNAL_ERROR);
            } else {
                boolean successful = answer.getBooleanProperty(PARAM_SUCCESSFUL);
                out.setSuccessful(successful);
                
    //          if (!successful)
                
                if (answer.getStringProperty("_error") != null)
                    out.setMsg(answer.getStringProperty("_error"));
                else
                    out.setMsg(answer.getStringProperty(PARAM_MSG));
                if (answer.getStringProperty(PARAM_RC) != null)
                    out.setReturnCode(answer.getIntProperty(PARAM_RC));
                
                if (successful) {
                    
                    if (answer instanceof BytesMessage) 
                        log.w("Receive byte message answer",queueName,operationName);
                    else
                    if (answer instanceof ObjectMessage) {
                        log.w("Receive serialized message answer",queueName,operationName);
                        throw new NotSupportedException("Receive object message answer");
                    }
                    
                    Object object = MJms.toObject(answer);
                    out.setResult(object);
                    
                }   
            }
            
            return out;
        }
        
    }
    
    /**
     * Return a list of possible operations from the other side. Could return null if a error occured.
     * @param con
     * @param queueName
     * @return A list of operations
     * @throws Exception
     */
    public static List<OperationDescription>  doGetOperationList(JmsConnection con, String queueName) throws Exception {
        IProperties pa = new MProperties();
        OperationResult ret = doExecuteOperation(con, queueName, "_list", "", pa, true);
        if (ret.isSuccessful()) {
            INode res = ret.getResultAsNode();
            String[] list = ((MProperties)res).getString("list","").split(",");
            LinkedList<OperationDescription> out = new LinkedList<>();
            for (String item : list) {
                OperationDescription desc = null;
                if (item.contains("|")) {
                    String[] parts = item.split("\\|");
                    try {
                        desc = newOperationDescription(UUID.fromString(parts[0]), parts[1], queueName, new Version(parts[2]), parts[3]);
                    } catch (Throwable t) {
                        log.d(queueName,item,t);
                    }
                } else {
                    desc = newOperationDescription(UUID.randomUUID(),item, queueName, Version.V_0_0_0, item);
                }
                if (desc != null)
                    out.add(desc);
            }
            return out;
        }
        return null;
    }

    public static OperationDescription newOperationDescription(UUID uuid, String path, String queue, Version version,
            String title) {
        OperationDescription desc = new OperationDescription(uuid, path, version, null, title);
        ((MProperties)desc.getLabels()).setString(Micro.LABEL_PROTO, Micro.PROTO_JMS);
        ((MProperties)desc.getLabels()).setString(LABEL_QUEUE, queue);
        return desc;
    }

}
