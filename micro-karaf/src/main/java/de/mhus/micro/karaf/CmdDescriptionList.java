package de.mhus.micro.karaf;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.micro.core.api.MicroFilter;
import de.mhus.micro.core.filter.FilterLike;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "micro",
        name = "description-list",
        description = "Show a list of known operation descriptions")
@Service
public class CmdDescriptionList extends AbstractCmd {

    @Argument(
            index = 0,
            name = "filter",
            required = false,
            description = "Filter Rules, first entry is a path rule, p: path, v: Verion, l: Label",
            multiValued = true)
    String[] rules;
    
    @Option(
            name = "-a",
            aliases = "--all",
            description = "Print all labels",
            required = false)
    private boolean full;

	@Override
	public Object execute2() throws Exception {
		MicroApiServiceProvider api = M.l(MicroApiServiceProvider.class);
		if (api == null) {
			System.out.println("MicroApiServiceProvider not found");
			return null;
		}
		
		MicroFilter filter = MicroFilter.ALL;
		if (filter != null) {
			filter = new FilterLike(rules);
		}
		
		ConsoleTable out = new ConsoleTable(tblOpt);
		out.setHeaderValues("PathVersion","Caption","Labels", "Uuid");
		api.getApi().discover(filter, desc ->
					{
						out.addRowValues(
								desc.getPathVersion(),
								desc.getCaption(),
								reduceLabels(desc.getLabels()),
								desc.getUuid());
						return Boolean.TRUE;
					}
				);

		out.print();
		return null;
	}

	private Object reduceLabels(IReadProperties labels) {
		if (full) return labels;
		MProperties p = new MProperties(labels);
		p.keys().removeIf(k -> k.startsWith("@") || k.startsWith("_") );
		return p;
	}

}
