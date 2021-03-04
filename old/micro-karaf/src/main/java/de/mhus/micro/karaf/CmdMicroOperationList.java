package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "mo-list", description = "Local Micro Operations list")
@Service
public class CmdMicroOperationList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        OperationsAdmin api = M.l(OperationsAdmin.class);
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Path","Version","Labels","Id");
        for (Operation oper : api.list()) {
            OperationDescription desc = oper.getDescription();
            out.addRowValues(desc.getPath(),desc.getVersionString(),desc.getLabels(),desc.getUuid());
        }
        out.print();
        return null;
    }

}
