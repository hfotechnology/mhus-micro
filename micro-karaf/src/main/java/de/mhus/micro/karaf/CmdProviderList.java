package de.mhus.micro.karaf;

import java.util.ArrayList;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "provider-list", description = "Provider list")
@Service
public class CmdProviderList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        MicroApi api = M.l(MicroApi.class);
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Provider","Path","Version","Labels","Id");
        
        for (MicroProvider provider : api.getProviders()) {
            ArrayList<OperationDescription> list = new ArrayList<>();
            provider.provided(list);
            if (list.isEmpty())
                out.addRowValues(provider.getClass().getCanonicalName(), "", "", "", "");
            else
                for (OperationDescription desc : list)
                    out.addRowValues(provider.getClass().getCanonicalName(), desc.getPath(), desc.getVersion(), desc.getLabels(), desc.getUuid());
        }
        
        out.print();
        return null;
    }

}
