package de.mhus.micro.karaf;

import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.MNode;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.filter.FilterPathVersion;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "micro",
        name = "description-info",
        description = "Show operation description information")
@Service
public class CmdDescriptionInfo extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id/pathVersion",
            required = true,
            description = "Ident of the operation",
            multiValued = false)
    String name;

    @Option(
            name = "-c",
            aliases = "--config",
            description = "Config parameters",
            required = false,
            multiValued = true
            )
    private String[] config;
    
	@Override
	public Object execute2() throws Exception {
		
		MicroApiServiceProvider api = M.l(MicroApiServiceProvider.class);
		if (api == null) {
			System.out.println("MicroApiServiceProvider not found");
			return null;
		}
        INode cfg = null;
        if (config != null) {
            cfg = new MNode();
            cfg.putAll( IProperties.explodeToMProperties(config) );
        }
        
		OperationDescription desc = api.getApi().first(new FilterPathVersion(name), cfg);
		if (desc == null) {
			System.out.println("Description not found");
			return null;
		}

		System.out.println("Name      : " + desc.getPathVersion());
		System.out.println("Path      : " + desc.getPath());
		System.out.println("Version   : " + desc.getVersion());
		System.out.println("Caption   : " + desc.getCaption());
		System.out.println("Title     : " + desc.getTitle());
		System.out.println("Id        : " + desc.getUuid());
		System.out.println("Parameters: " + desc.getParameterDefinitions());
		System.out.println("Form      : " + desc.getForm());
		System.out.println("Labels:");
		for (Entry<String, Object> label : desc.getLabels().entrySet())
			System.out.println("  " + label.getKey() + "=" + label.getValue());
		
		return null;
	}

}
