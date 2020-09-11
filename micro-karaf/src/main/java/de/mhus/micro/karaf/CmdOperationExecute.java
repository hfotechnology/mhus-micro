package de.mhus.micro.karaf;

import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroResult;
import de.mhus.micro.api.client.PathFilter;
import de.mhus.micro.api.client.UuidFilter;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "operation-execute", description = "Execute Operation")
@Service
public class CmdOperationExecute extends AbstractCmd {

    @Argument(
            index = 0,
            name = "path",
            required = true,
            description = "The path or UUID of the operation to execute",
            multiValued = false)
    String path;
    
    @Argument(
            index = 1,
            name = "version",
            required = false,
            description = "Version or version range or * for each",
            multiValued = false)
    String version;
    
    @Argument(
            index = 2,
            name = "arguments",
            required = false,
            description = "",
            multiValued = false)
    String arguments;
    
    @Argument(
            index = 3,
            name = "properties",
            required = false,
            description = "",
            multiValued = false)
    String properties;
    
    @Option(
            name = "-l",
            aliases = {"--label"},
            description = "Filter for labels",
            required = false,
            multiValued = true)
    private String[] labels;
    
    @Override
    public Object execute2() throws Exception {

        MicroApi api = M.l(MicroApi.class);
        
        IConfig eArguments = arguments == null ? new MConfig() : IConfig.readConfigFromString(arguments);
        IConfig eProperties = properties == null ? new MConfig() : IConfig.readConfigFromString(properties);
        
        IProperties eLabels = IProperties.explodeToMProperties(labels);
        
        MicroFilter filter = null;
        if (MValidator.isUUID(path))
            filter = new UuidFilter(UUID.fromString(path), eLabels);
        else {
            if ("*".equals(version)) version = null;
            filter = new PathFilter(path, version, eLabels);
        }
        
        MStopWatch timer = new MStopWatch(path).start();
        List<MicroResult> res = api.execute(filter, eArguments, eProperties);
        timer.stop();
        if (res.size() == 0)
            System.out.println("Operation not found");
        else
            System.out.println(res.size() + " operations executed in " + timer.getCurrentTimeAsString() );
        for (MicroResult r : res) {
            System.out.println(r.getDescription());
            System.out.println(r.getResult());
        }
        
        
        return null;
    }

}
