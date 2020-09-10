package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.server.MicroPusher;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "pusher-reload", description = "Reload pusher data")
@Service
public class CmdPusherReload extends AbstractCmd {

    @Argument(
            index = 0,
            name = "name",
            required = false,
            description = "",
            multiValued = false)
    String name;

    @Override
    public Object execute2() throws Exception {
        MicroApi api = M.l(MicroApi.class);
        for (MicroPusher pusher : api.getPushers()) {
            if (pusher.getClass().getCanonicalName().equals(name)) {
                System.out.println("Reload " + pusher.getClass().getCanonicalName());
                pusher.reload();
            }
        }
        return null;
    }

}
