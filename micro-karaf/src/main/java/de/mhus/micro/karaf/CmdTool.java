package de.mhus.micro.karaf;

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.MNode;
import de.mhus.micro.core.impl.AbstractApi;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "micro",
        name = "micro-tool",
        description = "Show micro info")
@Service
public class CmdTool extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command to info\n"
                            + " refresh\n"
                            + " reload\n"
                            + " check",
            multiValued = false)
    String cmd;

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
    
	@Override
	public Object execute2() throws Exception {
		MicroApiServiceProvider provider = M.l(MicroApiServiceProvider.class);
		if (provider == null) {
			System.out.println("MicroApiServiceProvider not found");
			return null;
		}
		
		AbstractApi api = (AbstractApi) provider.getApi();

        final INode cfg = config == null ? null : new MNode();
        if (cfg != null) {
            cfg.putAll( IProperties.explodeToMProperties(config) );
        }
		
		if (cmd.equals("info")) {
			api.forEachDiscovery(v -> {
				System.out.println("Discovery: " + v.getClass().getCanonicalName());
				v.discover(w -> {
					System.out.println("   " + w.getPathVersion() + " " + w.getUuid());
					return Boolean.TRUE;
				}, null, cfg);
			});
			api.forEachPublisher(v -> {
				System.out.println("Publisher: " + v.getClass().getCanonicalName());
			});
			api.forEachProvider(v -> {
				System.out.println("Provider : " + v.getClass().getCanonicalName());
				v.discover(w -> {
					System.out.println("   " + w.getPathVersion() + " " + w.getUuid());
					return Boolean.TRUE;
				}, null, cfg);
			});
			api.forEachProtocol(v -> {
				System.out.println("Protocol : " + MSystem.getCanonicalClassName(v.getClass()) + " " + Arrays.toString(v.getNames()));
			});
		}
		
		if (cmd.equals("refresh")) {
			api.forEachPublisher(v -> v.refresh() );
		}
		
		if (cmd.equals("reload")) {
			api.forEachDiscovery(v -> v.reload() );
		}

		if (cmd.equals("check")) {
			api.forEachDiscovery(v -> v.check() );
		}
		
		return null;
	}

}
