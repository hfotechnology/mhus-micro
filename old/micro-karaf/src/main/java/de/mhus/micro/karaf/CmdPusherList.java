package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.server.MicroPusher;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "pusher-list", description = "Operations list")
@Service
public class CmdPusherList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        MicroApi api = M.l(MicroApi.class);
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Name");
        
        for (MicroPusher pusher : api.getPushers()) {
            out.addRowValues(pusher.getClass().getCanonicalName());
        }
        out.print();
        return null;
    }

}
