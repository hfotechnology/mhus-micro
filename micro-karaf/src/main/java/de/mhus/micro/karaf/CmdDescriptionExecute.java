package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.MNode;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.filter.FilterPathVersion;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "micro",
        name = "description-execute",
        description = "Execute operation")
@Service
public class CmdDescriptionExecute extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id/pathVersion",
            required = true,
            description = "Ident of the operation",
            multiValued = false)
    String name;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Option(
            name = "-c",
            aliases = "--config",
            description = "Config parameters",
            required = false,
            multiValued = true
    		)
    private String[] config;
    
    @Option(
            name = "-t",
            aliases = "--try",
            description = "Try only mode",
            required = false)
    private boolean test = false;

    @Option(
            name = "-l",
            aliases = "--load",
            description = "First parameter is a json content",
            required = false)
    private boolean isLoad = false;
    
	@Override
	public Object execute2() throws Exception {
		
		MicroApiServiceProvider api = M.l(MicroApiServiceProvider.class);
		if (api == null) {
			System.out.println("MicroApiServiceProvider not found");
			return null;
		}
		OperationDescription desc = api.getApi().first(new FilterPathVersion(name));
		if (desc == null) {
			System.out.println("OperationDescription not found");
			return null;
		}
		
		INode param = null;
		if (isLoad) {
			param = INode.readNodeFromString(parameters[0]);
		} else {
			param = new MNode();
			param.putAll( IProperties.explodeToMProperties(parameters) );
		}
		
		INode cfg = null;
		if (config != null) {
			cfg = new MNode();
			cfg.putAll( IProperties.explodeToMProperties(config) );
		}
		
		DefaultTaskContext context = new DefaultTaskContext(this.getClass());
		context.setTestOnly(test);
		context.setParameters(param);
		context.setConfig(cfg);
		
		MicroResult res = api.getApi().execute(desc, param, cfg);
		
		System.out.println("Successful : " + res.isTransportSuccessful());
		System.out.println("Return Code: " +res.getReturnCode());
		System.out.println("Message    : " + res.getMessage());
		
		return res.getResult();
	}

}
