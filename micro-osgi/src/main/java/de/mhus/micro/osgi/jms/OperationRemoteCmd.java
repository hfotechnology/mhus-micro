package de.mhus.micro.osgi.jms;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.micro.jms.MicroJmsUtil;
import de.mhus.osgi.api.jms.JmsUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "operation-remote", description = "Operation commands")
@Service
public class OperationRemoteCmd extends AbstractCmd {

    @Argument(index=0, name="connection", required=true, description="JMS Connection name", multiValued=false)
    String connectionName = null;
    
    @Argument(index=1, name="queue", required=true, description="JMS Connection Queue OperationChannel", multiValued=false)
    String queueName = null;

	@Argument(index=2, name="cmd", required=true, description="Command list, bpm, info <path>, execute <path> [key=value]*", multiValued=false)
	String cmd;
	
	@Argument(index=3, name="path", required=false, description="Path to Operation", multiValued=false)
    String path;
	
    @Argument(index=4, name="version", required=false, description="Version of the Operation, e.g. 1.0.0", multiValued=false)
    String version;
    
	@Argument(index=5, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-c", aliases="--connection", description="JMS Connection Name",required=false)
	String conName = null;
	
	@Option(name="-t", aliases="--timeout", description="JMS answer timeout",required=false)
	long timeout = MPeriod.MINUTE_IN_MILLISECOUNDS / 2;
	
	@Option(name="-v", aliases="--verbose", description="Verbose output",required=false)
	boolean verbose = false;
	
	@Override
	public Object execute2() throws Exception {

		JmsConnection con = JmsUtil.getConnection(connectionName);
		if (conName != null)
			con = JmsUtil.getConnection(conName);
		
		if (cmd.equals("list")) {
			if (MString.isSet(path)) queueName = path;
			List<OperationDescription> list = MicroJmsUtil.doGetOperationList(con, queueName);
			if (list != null) {
				for (OperationDescription item : list)
					System.out.println(item);
				System.out.println("OK");
			} else {
				System.out.println("ERROR");
			}
		} else
		if (cmd.equals("info")) {
			
			IProperties pa = new MProperties();
			pa.setString("id", path);
			OperationResult ret = MicroJmsUtil.doExecuteOperation(con, queueName, "_get", version, pa, true, timeout);
			if (ret.isSuccessful()) {
				INode map = ret.getResultAsNode();
				System.out.println("Description  : " + map.get("group") + "," + map.get("id"));
				System.out.println("Form         : " + map.get("form") );
			} else {
				System.out.println("ERROR " + ret.getMsg());
			}
		} else
		if (cmd.equals("execute")) {
		    INode properties = INode.readNodeFromString(parameters);
			OperationResult res = MicroJmsUtil.doExecuteOperation(con, queueName, path, version, properties, true, timeout);
			System.out.println("Result: "+res);
			System.out.println("RC: " + res.getReturnCode());
			System.out.println("Object: " + resToString(res.getResultAsString()));
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String resToString(Object result) {
		if (result == null) return "null";
		if (result instanceof byte[]) {
			return new String((byte[])result);
		} else
		if (result instanceof Map) {
			StringBuffer out = new StringBuffer();
			Map map = (Map)result;
			out.append("Map:\n");
			for (Object key : new TreeSet<>(map.keySet() )) {
				Object val = map.get(key);
				out.append(key).append('=').append(val).append('\n');
			}
			return out.toString();
		}
			return result.toString();
	}

	
}
