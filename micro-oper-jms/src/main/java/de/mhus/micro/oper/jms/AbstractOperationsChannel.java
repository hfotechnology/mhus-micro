package de.mhus.micro.oper.jms;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.annotations.strategy.OperationChannel;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.Successful;
import de.mhus.lib.core.operation.util.MapValue;
import de.mhus.lib.core.pojo.DefaultFilter;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.pojo.PojoParser;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.util.SerializedValue;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.osgi.api.jms.AbstractJmsDataChannel;

public class AbstractOperationsChannel extends AbstractJmsDataChannel {

    public static final String PARAM_OPERATION_PATH = "path";
    public static final String PARAM_OPERATION_VERSION = "version";
    public static final String OPERATION_LIST = "_list";
    public static final String OPERATION_GET = "_get";
    public static final String PARAM_OPERATION_ID = "id";
    public static final String PARAM_TRACE = "trace";
    public static final String TRANSPORT_JMS = "jms";

    protected HashMap<String, Operation> operations = new HashMap<String, Operation>();

    public AbstractOperationsChannel() {
        connectionName = getConnectionName();
        log().i("Create OperationsChannel",getClass().getCanonicalName(), connectionName, getQueueName());
    }
    
    @Override
    protected JmsChannel createChannel() throws JMSException {
        return new ServerJms(new JmsDestination(getQueueName(), false)) {

            @Override
            public void receivedOneWay(Message msg) throws JMSException {
                AbstractOperationsChannel.this.received(msg, false);
            }

            @Override
            public Message received(Message msg) throws JMSException {
                return AbstractOperationsChannel.this.received(msg, true);
            }
            
        };
    }

    protected String getQueueName() {
        OperationChannel desc = getClass().getAnnotation(OperationChannel.class);
        if (desc != null) {
            return desc.name();
        }
        return getClass().getCanonicalName();
    }
    
    protected String getJmsConnectionName() {
        OperationChannel desc = getClass().getAnnotation(OperationChannel.class);
        String ret = null;
        if (desc != null)
            ret = desc.jmsConnection();
        
        if (MString.isEmpty(ret))
            ret = MJms.getDefaultConnectionName();
        
        return ret;
    }


    protected ServerJms getServer() {
        return (ServerJms) getChannel();
    };

    protected List<String> getOperations() {
        synchronized (operations) {
            ArrayList<String> out = new ArrayList<String>(operations.keySet());
            return out;
        }
    }

    protected OperationDescription getOperationDescription(String path, String version) {
        Operation oper = getOperation(path, version);
        if (oper == null) return null;
        return oper.getDescription();
    }

    protected Operation getOperation(String path, String version) {
        synchronized (operations) {
            return operations.get(path + "/" + version);
        }
    }

    protected void add(Operation operation) {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "de,java,org,com"); //HACK! !!!!
        
        OperationDescription desc2 = prepareDesc(operation.getDescription());
        synchronized (operations) {
            operations.put(desc2.getPath() + "/" + desc2.getVersionString(), operation);
        }
        MicroUtil.firePushAdd(desc2);
    }
    
    protected void remove(Operation operation) {
        OperationDescription desc = operation.getDescription();
        remove(desc.getPath() + "/" + desc.getVersionString());
    }
    
    protected void remove(String path) {
        Operation operation;
        synchronized (operations) {
            operation = operations.remove(path);
        }
        if (operation != null) {
            OperationDescription desc2 = prepareDesc(operation.getDescription());
            MicroUtil.firePushRemove(desc2);
        }
    }
    
    protected void clear() {
        for (String path : getOperations())
            remove(path);
    }

    private OperationDescription prepareDesc(OperationDescription desc) {
        OperationDescription desc2 = new OperationDescription(desc);
        desc2.putLabel(MicroConst.DESC_LABEL_TRANSPORT_TYPE, TRANSPORT_JMS);
        desc2.putLabel("queue", getQueueName());
        return desc2;
    }

    protected OperationResult doExecute(String path, String version, IConfig properties) {
        Operation oper = getOperation(path,version);
        if (oper == null) return new NotSuccessful(path,"not found",OperationResult.NOT_FOUND);
        DefaultTaskContext context = new DefaultTaskContext(this.getClass());
        context.setParameters(properties);
        try {
            return oper.doExecute(context);
        } catch (Throwable t) {
            log().d("doExecute",path,t);
            return new NotSuccessful(path, t.toString(), OperationResult.INTERNAL_ERROR);
        }
    }

    protected Message received(Message msg, boolean needResult) throws JMSException {
        
        log().d("Operation Call",msg);
        if (msg == null) return null;
        
        try {
            String path = msg.getStringProperty(PARAM_OPERATION_PATH);
            String version = msg.getStringProperty(PARAM_OPERATION_VERSION);
            if (path == null) return null;
            if (version == null) version = "0.0.0";
            
            IConfig properties = null;
            if (msg instanceof MapMessage)
                properties = MJms.getMapConfig((MapMessage)msg);
            else
            if (msg instanceof TextMessage) 
                properties = IConfig.readConfigFromString( ((TextMessage)msg).getText() );
            else
                throw new NotSupportedException("message type not supported",msg.getClass());
            
            OperationResult res = null;
            if (path.equals(OPERATION_LIST)) {
                String list = MString.join(getOperations().iterator(), ",");
                res = new Successful(OPERATION_LIST, "list",OperationResult.OK,"list",list);
            } else
            if (path.equals(OPERATION_GET)) {
                OperationDescription des = getOperationDescription(path, version);
                if (des == null)
                    res = new NotSuccessful(OPERATION_GET, "not found", OperationResult.NOT_FOUND);
                else {
                    res = new Successful(OPERATION_GET, "list",OperationResult.OK,
                            "path",des.getPath(),
                            "id",des.getUuid().toString(),
                            "form",String.valueOf( des.getForm() ),
                            "title",des.getTitle()
                            );
                }
            } else {
                String jwt = msg.getStringProperty("jwt_token");
                Subject subject = null;
                if (jwt != null) {
                    BearerToken token = new BearerToken(jwt);
                    subject = Aaa.getSubject();
                    subject.login(token);
                }
                try {
                    res = doExecute(path, version, properties);
                } finally {
                    if (subject != null)
                        subject.logout();
                }
            }
            if (!needResult) {
                log().d("Operation without result",path,res);
                return null;
            }
            
            Message ret = null;
            boolean consumed = false;
            if (res != null && res.getResult() != null && res.getResult() instanceof IConfig) {
                consumed = true;
                ret = getServer().createTextMessage();
                ret.setStringProperty("_encoding", "json");
                ((TextMessage)ret).setText( 
                        IConfig.toPrettyJsonString( (IConfig)res.getResult() )
                    );
            } else
            if (res != null && res.getResult() != null && res.getResult() instanceof Map) {
                // Map Message is allowed if all values are primitives. If not use object Message
                consumed = true;
                ret = getServer().createMapMessage();
                ret.setStringProperty("_encoding", "map");
                Map<?,?> map = (Map<?,?>)res.getResult();
                for (Map.Entry<?,?> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value == null || value.getClass().isPrimitive() || value instanceof String )
                        ((MapMessage)ret).setObject(String.valueOf(entry.getKey()), entry.getValue() );
                    else {
                        consumed = false;
                        ret = null;
                        break;
                    }
                }
            }

            if (consumed) {
                // already done
            } else
            if (res != null && res.getResult() != null) {
                if (res.getResult() instanceof SerializedValue) {
                    ret = getServer().createObjectMessage();
                    ret.setStringProperty("_encoding", "serialized value");
                    ((ObjectMessage)ret).setObject( ((SerializedValue)res.getResult()).getValue() );
                } else
                if (res.getResult() instanceof MapValue) {
                    ret = getServer().createMapMessage();
                    ret.setStringProperty("_encoding", "mapvalue");
                    MJms.setMapProperties((Map<?, ?>)((MapValue)res.getResult()).getValue(), (MapMessage)ret);
                } else
                if (res.getResult() instanceof Serializable ) {
                    ret = getServer().createObjectMessage();
                    ret.setStringProperty("_encoding", "serialized");
                    ((ObjectMessage)ret).setObject((Serializable) res.getResult());
                } else {
                    ret = getServer().createMapMessage();
                    ret.setStringProperty("_encoding", "pojo");
                    try {
                        IProperties prop = MPojo.pojoToProperties(res.getResult(), new PojoModelFactory() {

                            @Override
                            public PojoModel createPojoModel(Class<?> pojoClass) {
                                PojoModel model = new PojoParser().parse(pojoClass,"_",null).filter(new DefaultFilter(true, false, false, false, true) ).getModel();
                                return model;
                            }
                        } );
                        MJms.setMapProperties(prop, (MapMessage)ret);
                    } catch (IOException e) {
                        log().w(path,res,e);
                        ret.setStringProperty("_error", e.getMessage());
                    }
                }
            } else {
                ret = getServer().createTextMessage(null);
                ret.setStringProperty("_encoding", "empty");
            }
            
            if (res == null) {
                ret.setIntProperty("rc", OperationResult.INTERNAL_ERROR);
                ret.setStringProperty("msg", "null");
                ret.setBooleanProperty("successful", false);
            } else {
                ret.setIntProperty("rc", res.getReturnCode());
                ret.setStringProperty("msg", res.getMsg());
                ret.setBooleanProperty("successful", res.isSuccessful());
                OperationDescription next = res.getNextOperation();
                if (next != null) {
                    ret.setStringProperty("next.path", next.getPath());
                }
            }
            ret.setStringProperty("path", path);
            // for bpm calls
            if (msg instanceof MapMessage) {
                MapMessage map = (MapMessage)msg;
                String value = map.getString(PARAM_TRACE);
                if (value != null)
                    ret.setStringProperty(PARAM_TRACE, value);
            }
            
            log().d("Operation Result",res,ret);
            
            return ret;
            
        } catch (Throwable t) {
            log().d("Operation Exception",t);
            TextMessage ret = getServer().createTextMessage(null);
            ret.setIntProperty("rc", -1);
            ret.setStringProperty("_encoding", "error");
            ret.setText(t.toString());
        }
        
        return null;
    }

}
