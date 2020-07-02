package de.mhus.micro.docker;

import java.io.File;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.amihaiemil.docker.Container;
import com.amihaiemil.docker.Containers;
import com.amihaiemil.docker.UnixDocker;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "dev-docker", description = "Docker")
@Service
public class CmdDocker extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "",
            multiValued = false)
    String cmd;
    
    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {
        
        if (cmd.equals("test")) {
            Containers list = new UnixDocker(new File("/var/run/docker.sock"))
                    .containers();
            for (Container c : list)
                System.out.println(c.containerId());
         }
        
        return null;
    }

}
