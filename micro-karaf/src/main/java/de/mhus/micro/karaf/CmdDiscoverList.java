package de.mhus.micro.karaf;

import java.util.ArrayList;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "operation-discover", description = "Operations list")
@Service
public class CmdDiscoverList extends AbstractCmd {

    @Argument(
            index = 0,
            name = "filter",
            required = false,
            description = "",
            multiValued = false)
    String filter;

    @Override
    public Object execute2() throws Exception {
        MicroApi api = M.l(MicroApi.class);
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Path","Version","Labels","Id");
        ArrayList<OperationDescription> results = new ArrayList<>();
        MicroFilter f = new MicroFilter() {
            @Override
            public boolean matches(OperationDescription desc) {
                return filter == null || desc.getPath().matches(filter);
            }
        };
        api.discover(f, results);
        for (OperationDescription desc : results) {
            out.addRowValues(desc.getPath(),desc.getVersionString(),desc.getLabels(),desc.getUuid());
        }
        out.print();
        return null;
    }

}
