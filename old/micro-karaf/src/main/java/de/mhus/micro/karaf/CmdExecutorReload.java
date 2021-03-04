package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.micro.api.MicroApi;
import de.mhus.micro.api.client.MicroExecutor;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "micro", name = "executor-reload", description = "Reload executor data")
@Service
public class CmdExecutorReload extends AbstractCmd {

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
        for (MicroExecutor item : api.getExecutors()) {
            if (name == null || item.getClass().getCanonicalName().equals(name)) {
                System.out.println("Reload " + item.getClass().getCanonicalName());
                item.reload();
            }
        }
        return null;
    }

}
