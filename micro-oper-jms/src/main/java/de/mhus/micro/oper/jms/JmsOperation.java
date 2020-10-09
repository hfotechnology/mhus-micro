package de.mhus.micro.oper.jms;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.api.client.MicroOperation;
import de.mhus.micro.api.client.MicroResult;
import de.mhus.osgi.api.jms.JmsUtil;

public class JmsOperation extends MLog  implements MicroOperation {

    private OperationDescription desc;

    public JmsOperation(OperationDescription desc) {
        this.desc = desc;
    }

    @Override
    public MicroResult execute(IConfig arguments, IProperties properties) throws Exception {
        String queue = desc.getLabels().get("queue");
        try (JmsDestination dest = new JmsDestination(queue, false)) {
    
            String conName = MJms.getDefaultConnectionName();
            JmsConnection con = JmsUtil.getConnection( conName );
            if (con == null) 
                throw new NotFoundException("jms connection not found", conName);
            dest.setConnection(con);
            try (ClientJms client = new ClientJms(dest)) {
                
                TextMessage msg = con.createTextMessage();
                msg.setStringProperty(AbstractOperationsChannel.PARAM_OPERATION_PATH, desc.getPath());
                msg.setStringProperty(AbstractOperationsChannel.PARAM_OPERATION_VERSION, desc.getVersionString());
                String json = IConfig.toPrettyJsonString(arguments);
                msg.setText(json);
                Message res = client.sendJms(msg);
                
                int rc = res.getIntProperty("rc");
                String mesg = res.getStringProperty("msg");
                boolean successful = res.getBooleanProperty("successful");
                
                IConfig result = null;
                if (res instanceof TextMessage) {
                    result = IConfig.readConfigFromString( ((TextMessage)res).getText() );
                } else
                if (res instanceof MapMessage) {
                    result = MJms.getMapConfig((MapMessage)res);
                }
    
                return new MicroResult(successful, rc, mesg, desc, result);
    
            }
        }
    }

    @Override
    public OperationDescription getDescription() {
        return desc;
    }

}
