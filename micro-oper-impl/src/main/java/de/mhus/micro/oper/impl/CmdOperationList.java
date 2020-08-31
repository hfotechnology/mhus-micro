package de.mhus.micro.oper.impl;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "mo-list", description = "Operations list")
@Service
public class CmdOperationList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        OperationsAdmin api = M.l(OperationsAdmin.class);
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Path","Version","Parameters");
        for (Operation oper : api.list()) {
            OperationDescription desc = oper.getDescription();
            out.addRowValues(desc.getPath(),desc.getVersionString(),desc.getParameters());
        }
        out.print();
        return null;
    }

}
